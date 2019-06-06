package com.jike.balance.server;

/**
 *  修改服务器的负载信息
 *
 */
public interface BalanceUpdateProvider {
	
	public boolean addBalance(Integer step);
	
	public boolean reduceBalance(Integer step);

}
