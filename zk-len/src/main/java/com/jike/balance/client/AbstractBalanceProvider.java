package com.jike.balance.client;

import java.util.List;

public abstract class AbstractBalanceProvider<T> implements BalanceProvider<T> {
	
	//负载均衡算法 获取负载最小的Server
	protected abstract T balanceAlgorithm(List<T> items);
	
	//获取资源列表
	protected abstract List<T> getBalanceItems();
	
	//
	public T getBalanceItem(){
		List<T> balances = getBalanceItems();
		T balance = balanceAlgorithm(balances);
		return balance;
	}

}
