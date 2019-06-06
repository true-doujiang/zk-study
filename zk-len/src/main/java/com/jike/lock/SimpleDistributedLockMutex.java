package com.jike.lock;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 *	
 */
public class SimpleDistributedLockMutex extends BaseDistributedLock implements DistributedLock {
	
	//锁名称前缀   成功创建的顺序节点名称如: lock-000000000000,lock-000000000001,....
	private static final String LOCK_NAME = "lock-";
	
	//zookeeper中locker节点的路径 如: /locker
	private final String basePath;
	
	//获取锁以后自己创建的那个顺序节点路径
	private String ourLockPath;
    

    /**
     * @param client 	操作zookeeper
     * @param basePath	zookeeper的锁的根路径，也就是ppt架构图的locker节点
     */
    public SimpleDistributedLockMutex(ZkClientExt client, String basePath){
    	super(client, basePath, LOCK_NAME);
    	this.basePath = basePath;
    }

    
    /**
     * 	获取锁直到超时
     */
	public void acquire() throws Exception {
        if ( !internalLock(-1, null) ) {
        	//没有获得抛出异常
            throw new IOException("连接丢失!在路径:'"+basePath+"'下不能获取锁!");
        }
	}

	public boolean acquire(long time, TimeUnit unit) throws Exception {
		return internalLock(time, unit);
	}

	/**
     * 	释放锁
     */
	public void release() throws Exception {
		releaseLock(ourLockPath);
	}

    /**
     * 	用于获取锁资源   成功true   不成功false
     */
    private boolean internalLock(long time, TimeUnit unit) throws Exception {
    	ourLockPath = attemptLock(time, unit);     	//成功获取锁就返回一个节点路径
        return ourLockPath != null;
    }
}
