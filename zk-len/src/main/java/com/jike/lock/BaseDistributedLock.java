package com.jike.lock;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

/**
 *	zookeeper实现分布式锁的细节
 */
public class BaseDistributedLock {
	
    private final ZkClientExt client;
    private final String path;
    private final String basePath;
    private final String lockName;
    
    //网络闪断重试次数
    private static final Integer MAX_RETRY_COUNT = 10; 
    	
    
	public BaseDistributedLock(ZkClientExt client, String path, String lockName){
        this.client = client;
        this.basePath = path;
        this.path = path.concat("/").concat(lockName);		
		this.lockName = lockName;
	}
	
	
    /**
	 * 释放锁
	 */
	protected void releaseLock(String lockPath) throws Exception{
		deleteOurPath(lockPath);	
	}
	
	/**
	 * 尝试获取锁的具体实现
	 */
	protected String attemptLock(long time, TimeUnit unit) throws Exception{
		
        final long      startMillis = System.currentTimeMillis();
        final Long      millisToWait = (unit != null) ? unit.toMillis(time) : null;

        String          ourPath = null;
        boolean         hasTheLock = false;
        boolean         isDone = false;
        int             retryCount = 0;
        
        //网络闪断需要重试一试
        while ( !isDone ) {
            isDone = true;
            try {
            	//函数一   在/locker 下创建临时的顺序节点
                ourPath = createLockNode(client, path);
                //函数二	判断自己是否获得了锁，如果没有获得那么我们等待直到获取锁或者超时 
                hasTheLock = waitToLock(startMillis, millisToWait, ourPath);
            } catch ( ZkNoNodeException e ) {
                if ( retryCount++ < MAX_RETRY_COUNT ) {
                    isDone = false;
                }  else {
                    throw e;
                }
            }
        }
        
        if ( hasTheLock ) {
            return ourPath;
        }

        return null;
	}
	

	/**
	 * 函数二	判断自己是否获得了锁，如果没有获得那么我们等待直到获取锁或者超时 
	 * @param startMillis
	 * @param millisToWait
	 * @param ourPath
	 * @return
	 * @throws Exception
	 */
	private boolean waitToLock(long startMillis, Long millisToWait, String ourPath) throws Exception{
		boolean  haveTheLock = false;
        boolean  doDelete = false;
        try {
            while ( !haveTheLock ) {
            	//获取根节点下(/Mutex)所有的节点(locker-....)  从小到大排序
                List<String> children = getSortedChildren();
                String sequenceNodeName = ourPath.substring(basePath.length()+1);

                //如果这里居然没有找到我们自己刚刚创建的节点    说明在调用 getSortedChildren() 之前由于网络闪断把我们刚刚创建的那个节点删掉了，此时要抛出异常,给上级处理
                int  ourIndex = children.indexOf(sequenceNodeName);
                if ( ourIndex < 0 ) {
                	throw new ZkNoNodeException("节点没有找到: " + sequenceNodeName);  //下面catch住 又抛出去了 throw e;
                }

                boolean isGetTheLock = ourIndex == 0; //如果排在第0个位置   说明已经获得了锁
                String  pathToWatch = isGetTheLock ? null : children.get(ourIndex - 1);

                if ( isGetTheLock ){
                    haveTheLock = true;
                } else {
                	/**
                	 * else里大的逻辑是   订阅自己小的那个节点的删除事件
                	 */
                    String  previousSequencePath = basePath .concat( "/" ) .concat(pathToWatch);
                    final CountDownLatch latch = new CountDownLatch(1);
                    
                    final IZkDataListener previousListener = new IZkDataListener() {
                    	
                		public void handleDataDeleted(String dataPath) throws Exception {
                			latch.countDown();			
                		}
                		public void handleDataChange(String dataPath, Object data) throws Exception {
                			// ignore									
                		}
                	};

                    try {
						//如果节点不存在会出现异常
                    	client.subscribeDataChanges(previousSequencePath, previousListener);
                    	
                        if ( millisToWait != null ) {
                            millisToWait -= (System.currentTimeMillis() - startMillis);
                            startMillis = System.currentTimeMillis();
                            if ( millisToWait <= 0 ) {
                                doDelete = true;    // timed out - delete our node   我本来说等5s的 ，结果代码执行到这里  都尼玛超过5s了   直接就break了
                                break;	//跳出while
                            }
                            //这个方法会阻塞     阻塞 millisToWait毫秒   这里虽然第一次出不去while但是再次进来会从break跳出去
                            latch.await(millisToWait, TimeUnit.MICROSECONDS); 
                        } else {
                        	latch.await(); //这个方法会阻塞   直到latch=0时才放行
                        }
                    } catch ( ZkNoNodeException e ) {
                        //ignore
                    } finally{
                    	client.unsubscribeDataChanges(previousSequencePath, previousListener);
                    }
                }
            } //while
        } catch ( Exception e )  {
            //发生异常需要删除节点
            doDelete = true;
            throw e;
        } finally {
            //如果需要删除节点
            if ( doDelete ) {
                deleteOurPath(ourPath);
            }
        }
        return haveTheLock;
	}
	

	
    private List<String> getSortedChildren() throws Exception {
    	try{
	        List<String> children = client.getChildren(basePath);
	        
	        Collections.sort(children,  new Comparator<String>() {
	                public int compare(String lhs, String rhs) {
	                	int i = getLockNodeNumber(lhs, lockName).compareTo(getLockNodeNumber(rhs, lockName));
	                    return i;
	                }
	            }
	        );
	        
	        return children;
	        
    	}catch(ZkNoNodeException e){
    		client.createPersistent(basePath, true);
    		return getSortedChildren();
    	}
    }
    
    private String getLockNodeNumber(String str, String lockName) {
        int index = str.lastIndexOf(lockName);
        if ( index >= 0 ) {
            index += lockName.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }

	/**
	 * 删除zookeeper中指定的节点
	 */
	private void deleteOurPath(String ourPath) throws Exception{
		client.delete(ourPath);
	}
	
	/**
	 * 在zookeeper中创建临时的顺序节点
	 */
	private String createLockNode(ZkClient client,  String path) throws Exception{
		return client.createEphemeralSequential(path, null);
	}
}
