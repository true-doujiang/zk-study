package com.jike.balance.server;

import java.util.ArrayList;
import java.util.List;


/**
 * 调度类: 启动服务器
 *
 */
public class ServerRunner {
	
    private static final int  SERVER_QTY = 2;
    private static final String  ZOOKEEPER_SERVER = "192.168.254.138:2181";    
    private static final String  SERVERS_PATH = "/servers";
	
	public static void main(String[] args) {
		
		List<Thread> threadList = new ArrayList<Thread>(); 
				
		/**
		 * 	启动俩个服务器    每个服务器在一个单独的线程中
		 */
		for(int i=0; i<SERVER_QTY; i++){
			final Integer count = i;
			Thread thread = new Thread(new Runnable() {
				public void run() {		
					ServerData sd = new ServerData();
					sd.setBalance(0);
					sd.setHost("127.0.0.1");
					sd.setPort(6000+count);
					Server server = new ServerImpl(ZOOKEEPER_SERVER,SERVERS_PATH,sd);
					server.bind();					
				}
			});		
			
			threadList.add(thread);
			thread.start();
		}
		
		for (int i=0; i<threadList.size(); i++){
			try {
				threadList.get(i).join();
			} catch (InterruptedException ignore) {
				//
			}
		}
	}

}
