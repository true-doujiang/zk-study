package com.jike.balance.client;


/**
 * 提供基于负载均衡的算法
 */
public interface BalanceProvider<T> {
	
	public T getBalanceItem();
	

}
