package com.jike.balance.client;

/**
 *	每个客户端都要实现我
 */
public interface Client {

	public void connect() throws Exception;
	public void disConnect() throws Exception;
	
}
