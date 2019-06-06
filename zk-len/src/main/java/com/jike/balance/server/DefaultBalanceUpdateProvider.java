package com.jike.balance.server;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkBadVersionException;
import org.apache.zookeeper.data.Stat;

public class DefaultBalanceUpdateProvider implements BalanceUpdateProvider {

	//WorkServer在zookeeper中创建的临时节点路径
	private String serverPath;
	private ZkClient zc;

	public DefaultBalanceUpdateProvider(String serverPath, ZkClient zkClient) {
		this.serverPath = serverPath;
		this.zc = zkClient;
	}

	public boolean addBalance(Integer step) {
		Stat stat = new Stat();
		ServerData sd = null;
		
		//不断重试知道成功返回true  或者false
		while (true) {
			try {
				sd = zc.readData(this.serverPath, stat);
				sd.setBalance(sd.getBalance() + step);
				
				//版本不对就会抛ZkBadVersionException
				zc.writeData(this.serverPath, sd, stat.getVersion());
				return true;
			} catch (ZkBadVersionException e) {
				// ignore
			} catch (Exception e) {
				return false;
			}
		}
	}

	public boolean reduceBalance(Integer step) {
		Stat stat = new Stat();
		ServerData sd = null;
		
		while (true) {
			try {
				sd = zc.readData(this.serverPath, stat);
				final Integer currBalance = sd.getBalance();
				sd.setBalance(currBalance>step ? currBalance-step : 0); //版本最小值为0
				
				zc.writeData(this.serverPath, sd, stat.getVersion());
				return true;
			} catch (ZkBadVersionException e) {
				// ignore
			} catch (Exception e) {
				return false;
			}
		}
	}

}
