package com.jike.subscribe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

/**
 * SubscribeZkClient程序的入口负责启动 WorkServer ManageServer
 * 演示：
 * 1、启动本类
 * 2、./zkCli.sh -server 192.168.254.138:2181
 * 3、create /command list|create|modify
 * 	  delete /servers/节点
 * 
 * 	 set /config newconfig    这么修改配置WorkServer怎么就监控不到呢????   原因我把异常吞掉了，所以没看到
 */
public class SubscribeZkClient {
	
	   //5个WorkServer
	   private static final int  CLIENT_QTY = 5;

	    private static final String  ZOOKEEPER_SERVER = "192.168.254.138:2181";
	    
	    private static final String  CONFIG_PATH = "/config";
	    private static final String  COMMAND_PATH = "/command";
	    private static final String  SERVERS_PATH = "/servers";
	       
	    
	    public static void main(String[] args) throws Exception {

	        List<ZkClient>  clients = new ArrayList<ZkClient>();
	        List<WorkServer>  workServers = new ArrayList<WorkServer>();
	        try {
	        	//默认的ServerConfig
	        	ServerConfig initConfig = new ServerConfig();
	        	initConfig.setDbPwd("ok");
	        	initConfig.setDbUrl("jdbc:mysql://localhost:3306/mydb");
	        	initConfig.setDbUser("root");
	        	
	        	ZkClient clientManage = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new BytesPushThroughSerializer());
	        	
	        	//创建ManageServer
	        	ManageServer manageServer = new ManageServer(SERVERS_PATH, COMMAND_PATH,CONFIG_PATH, clientManage, initConfig);
	        	manageServer.start();
	        		        	
	            for ( int i = 0; i < CLIENT_QTY; ++i ) {
	                ZkClient client = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new BytesPushThroughSerializer());
	                clients.add(client);
	                
	                //WorkServer的基本信息
	                ServerData serverData = new ServerData();
	                serverData.setId(i);
	                serverData.setName("WorkServer#"+i);
	                serverData.setAddress("192.168.254."+i);

	                WorkServer  workServer = new WorkServer(CONFIG_PATH, SERVERS_PATH, serverData, client, initConfig);
	                workServers.add(workServer);
	                workServer.start();	                
	            }	
	            
	            System.out.println("敲回车键退出！\n");
	            new BufferedReader(new InputStreamReader(System.in)).readLine();
	            
	        } finally {
	            System.out.println("Shutting down...");

	            for ( WorkServer workServer : workServers ) {
	            	try {
	            		workServer.stop();
					} catch (Exception e) {
						e.printStackTrace();
					}           	
	            }
	            
	            for ( ZkClient client : clients ) {
	            	try {
	            		client.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
	        }
	    }	

}
