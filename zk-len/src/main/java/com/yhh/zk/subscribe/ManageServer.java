package com.yhh.zk.subscribe;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import com.alibaba.fastjson.JSON;

/**
 *  a.可以通过Config节点下发配置信息。
 *  b.当做Monitor,通过监控Servers节点的子节点列表的改变来更新自己内存中工作服务器列表的信息
 */
public class ManageServer {

	//用于监控server节点的变化
	private String serversPath;

	//订阅common节点的信息
	private String commandPath;

	//向config节点发送配置信息
	private String configPath;

	private ZkClient zkClient;

	//配置信息
	private ServerConfig config;

	//监控server节点的监听器
	private IZkChildListener childListener;

	//监控common节点的监听器
	private IZkDataListener dataListener;

	//工作节点列表
	private List<String> workServerList;
	

	/**
	 * @param config 给WorkServer用的默认配置
	 */
	public ManageServer(String serversPath, String commandPath, String configPath, ZkClient zkClient, ServerConfig config) {
		this.serversPath = serversPath;
		this.commandPath = commandPath;
		this.configPath = configPath;
		this.zkClient = zkClient;
		this.config = config;
		
		this.childListener = new IZkChildListener() {

			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				workServerList = currentChilds;	//当/servers节点变化时只是记录在workServerList属性中   并没有添加到zookeeper的/servers节点
				System.out.println("ManageServer检测到workServer list changed, new list is: " + currentChilds);
			}
		};
		
		this.dataListener = new IZkDataListener() {

			public void handleDataChange(String dataPath, Object data) throws Exception {
				String cmd = new String((byte[]) data);
				System.out.println("cmd:" + cmd);
				exeCmd(cmd);
			}

			public void handleDataDeleted(String dataPath) throws Exception {
				// ignore;
			}
		};
	}

	public void start() {
		initRunning();
	}

	public void stop() {
		zkClient.unsubscribeChildChanges(serversPath, childListener);
		zkClient.unsubscribeDataChanges(commandPath, dataListener);
	}
	
	private void initRunning() {
		//订阅common节点的信息		用于接收controlServer发送的命令
		zkClient.subscribeDataChanges(commandPath, dataListener);
		//订阅server节点的信息		用于监控工作列表的变化
		zkClient.subscribeChildChanges(serversPath, childListener);
	}

	/* 执行控制命令
	 * 1: list   列出workServerList
	 * 2: create 用于在zookeeper中创建config节点
	 * 3: modify 用于修改config节点的内容	也就相当于修改workServer的配置
	 */
	private void exeCmd(String cmdType) {
		if ("list".equals(cmdType)) {
			execList();
		} else if ("create".equals(cmdType)) {
			execCreate();
		} else if ("modify".equals(cmdType)) {
			execModify();
		} else {
			System.err.println("error command!" + cmdType);
		}
	}

	private void execList() {
		System.out.println(workServerList.toString());
	}

	/**
	 * 创建/config 并写入初始配置信息
	 */
	private void execCreate() {
		if (!zkClient.exists(configPath)) {
			//不存在才会进来
			try {
				zkClient.createPersistent(configPath, JSON.toJSONString(config).getBytes());
				System.err.println("ManageServer创建/config节点, 并写入数据:" + config); 
			} catch (ZkNodeExistsException e) {
				//节点已存在直接写入配置信息
				zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
			} catch (ZkNoNodeException e) {
				//表示config节点中的一个父节点还没有创建   现在创建它
				String parentDir = configPath.substring(0, configPath.lastIndexOf('/'));  //拿到父节点路径
				zkClient.createPersistent(parentDir, true);
				execCreate();
			}
		}
	}

	/**
	 * 修改/config 配置信息
	 */
	private void execModify() {
		config.setDbUser(config.getDbUser() + "_modify");
		try {
			zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
		} catch (ZkNoNodeException e) {
			System.err.println("ManageServer修改/config节点数据(修改配置信息)异常, 因为/config节点还没有创建.... "); 
			execCreate();
		}
	}

	
}
