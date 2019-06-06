package com.jike.subscribe;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import com.alibaba.fastjson.JSON;

/**
 * 
 */
public class WorkServer {

	private ZkClient zkClient;
	
	//zookeeper中的config节点路径
	private String configPath;
	
	//zookeeper中的server节点路径
	private String serversPath;
	
	//WorkServer的基本信息
	private ServerData serverData;
	
	//WorkServer的配置信息    由ManageServer将ServerConfig对象转成json字符串传入的
	private ServerConfig serverConfig;
	
	//订阅/config节点的数据监听器
	private IZkDataListener dataListener;

	/**
	 * @param initConfig 当前工作服务器的初始配置
	 */
	public WorkServer(String configPath, String serversPath, ServerData serverData, ZkClient zkClient, ServerConfig initConfig) {
		this.configPath = configPath;
		this.serversPath = serversPath;
		this.serverData = serverData;
		this.zkClient = zkClient;
		this.serverConfig = initConfig;

		this.dataListener = new IZkDataListener() {

			public void handleDataChange(String dataPath, Object data) throws Exception {
				try {
					String retJson = new String((byte[])data);
					ServerConfig serverConfigLocal = (ServerConfig)JSON.parseObject(retJson,ServerConfig.class);
					updateConfig(serverConfigLocal);
					System.out.println("new workServer config is:" + serverConfig.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}

			public void handleDataDeleted(String dataPath) throws Exception {

			}
		};
	}

	public void start() {
		System.out.println("workServer start...");
		initRunning();
	}

	public void stop() {
		System.out.println("workServer stop...");
		zkClient.unsubscribeDataChanges(configPath, dataListener);
	}

	private void initRunning() {
		registMe();
		System.out.println(configPath); 
		zkClient.subscribeDataChanges(configPath, dataListener); //订阅/config节点的数据监听器
	}
	
	/**
	 * 工作服务器启动时在/servers节点注册自己
	 */
	private void registMe() {
		//注册  其实就是在server节点下创建一个临时节点
		String mePath = serversPath.concat("/").concat(serverData.getAddress());
		try {
			byte[] pathBytes = JSON.toJSONString(serverData).getBytes();
			zkClient.createEphemeral(mePath, pathBytes); //临时节点(临时注册)
		} catch (ZkNoNodeException e) {
			System.err.println("WorkServer要注册到 /servers节点   但是/servers节点还没有创建...");
			//serversPath节点如果没有创建的话   你怎么能直接创建他的子节点呢
			zkClient.createPersistent(serversPath, true);  //创建一个持久节点
			registMe();
		}
	}

	private void updateConfig(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}

}
