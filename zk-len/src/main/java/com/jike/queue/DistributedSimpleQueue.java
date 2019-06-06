package com.jike.queue;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;




import org.I0Itec.zkclient.ExceptionUtil;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

public class DistributedSimpleQueue<T> {

	protected final ZkClient zkClient;
	protected final String root;

	protected static final String Node_NAME = "n_";


	public DistributedSimpleQueue(ZkClient zkClient, String root) {
		this.zkClient = zkClient;
		this.root = root;
	}

	/**
	 *  获取队列的大小
	 */
	public int size() {
		return zkClient.getChildren(root).size();
	}

	/**
	 *  判断队列是否为空
	 */
	public boolean isEmpty() {
		return zkClient.getChildren(root).size() == 0;
	}
	
	/**
	 *  向队列提交数据： 只要在/queue下创建持久顺序节点 并把需要存放的数据一同写到该顺序节点中
	 */
    public boolean offer(T element) throws Exception {
    	
    	String nodeFullPath = root .concat("/").concat(Node_NAME);
        try {
            zkClient.createPersistentSequential(nodeFullPath , element); //持久顺序节点
        } catch (ZkNoNodeException e) {
        	zkClient.createPersistent(root);
        	offer(element);
        } catch (Exception e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }
        return true;
    }

    /**
	 *  从队列取出数据   这个方法不够强  应该是：取不到数据就让它一直阻塞  所以DistributedBlockingQueue重写了该方法
	 */
	@SuppressWarnings("unchecked")
	public T poll() throws Exception {
		try {
			List<String> list = zkClient.getChildren(root);
			if (list.size() == 0) {
				return null;
			}
			
			Collections.sort(list, new Comparator<String>() {
				public int compare(String lhs, String rhs) {
					return getNodeNumber(lhs, Node_NAME).compareTo(getNodeNumber(rhs, Node_NAME));
				}
			});
			
			for ( String nodeName : list ){
				String nodeFullPath = root.concat("/").concat(nodeName);	
				try {
					//这2步 只要有一个出现异常说明获取不成功   继续循环获取
					T node = (T) zkClient.readData(nodeFullPath);
					zkClient.delete(nodeFullPath);
					return node;
				} catch (ZkNoNodeException e) {
					// ignore
				}
			}
			return null;
		} catch (Exception e) {
			throw ExceptionUtil.convertToRuntimeException(e);
		}
	}

	private String getNodeNumber(String str, String nodeName) {
		int index = str.lastIndexOf(nodeName);
		if (index >= 0) {
			index += Node_NAME.length();
			return index <= str.length() ? str.substring(index) : "";
		}
		return str;
	}

}
