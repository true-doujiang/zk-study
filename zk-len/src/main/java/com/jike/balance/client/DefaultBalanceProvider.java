package com.jike.balance.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import com.jike.balance.server.ServerData;

/**
 * 读取servers节点的列表
 * @author youhuan
 *
 */
public class DefaultBalanceProvider extends AbstractBalanceProvider<ServerData> {

	//zookeeper服务器地址
	private final String zkServer;
	private final String serversPath;
	private final ZkClient zc;
	
	private static final Integer SESSION_TIME_OUT = 10000;
	private static final Integer CONNECT_TIME_OUT = 10000;

	
	public DefaultBalanceProvider(String zkServer, String serversPath) {
		this.zkServer = zkServer;
		this.serversPath = serversPath;

		this.zc = new ZkClient(this.zkServer, SESSION_TIME_OUT, CONNECT_TIME_OUT, new SerializableSerializer());
	}

	/**
	 * 负载均衡算法：
	 * 按照items的负载信息从小到大排序    返回最下的
	 * 
	 * ServerData implements Comparable<ServerData>  重写compareTo
	 */
	@Override
	protected ServerData balanceAlgorithm(List<ServerData> items) {
		if (items.size()>0){
			Collections.sort(items);
			return items.get(0);
		}else{
			return null;
		}
	}

	@Override
	protected List<ServerData> getBalanceItems() {
		List<ServerData> sdList = new ArrayList<ServerData>();
		List<String> children = zc.getChildren(this.serversPath);
		
		for(int i=0; i<children.size(); i++){
			ServerData sd = zc.readData(serversPath+"/"+children.get(i));
			sdList.add(sd);
		}
		return sdList;
	}

}
