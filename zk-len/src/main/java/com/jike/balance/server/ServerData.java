package com.jike.balance.server;

import java.io.Serializable;

/**
 * 服务端与客户端公用类
 * 服务端会把自己的基本信息包括负载打包成ServerData 保存到zookeeper中
 * 客户端在计算负载的时候会去zookeeper中拿ServerData 并取得服务端的地址和他的负载信息
 */
public class ServerData implements Serializable,Comparable<ServerData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8892569870391530906L;
	
	
	private Integer balance;
	private String host;
	private Integer port;
	
	
	public Integer getBalance() {
		return balance;
	}
	public void setBalance(Integer balance) {
		this.balance = balance;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	
	public int compareTo(ServerData o) {
		return this.getBalance().compareTo(o.getBalance());
	}
	
	@Override
	public String toString() {
		return "ServerData [balance=" + balance + ", host=" + host + ", port=" + port + "]";
	}
	
	

}
