package com.jike.balance.server;

/**
 *	服务端启动时注册到zookeeper用的接口
 */
public interface RegistProvider {
	
	public void regist(Object context) throws Exception;
	
	public void unRegist(Object context) throws Exception;

}
