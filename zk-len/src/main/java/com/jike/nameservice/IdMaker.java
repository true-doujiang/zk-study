package com.jike.nameservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

public class IdMaker {
	
	
	private ZkClient client = null;
	//zookeeper的ip
	private final String server;	
	//记录父节点的路径
	private final String root;	
	//顺序节点的名称
	private final String nodeName;
	//标识当前服务是否正在运行
	private volatile boolean running = false;
	
	private ExecutorService cleanExector = null;
	
	/**
	 * 不删除 立即删除 延迟删除
	 */
	public enum RemoveMethod {
		NONE, IMMEDIATELY, DELAY
	}
	
	public IdMaker(String zkServer,String root,String nodeName){
		this.server = zkServer;
		this.root = root;
		this.nodeName = nodeName;
	}
	
	public void start() throws Exception {
		if (running) {
			throw new Exception("server has stated...");
		}
		running = true;
		init();
	}
	
	
	public void stop() throws Exception {
		if (!running) {
			throw new Exception("server has stopped...");
		}
		running = false;
		freeResource();
	}
	
	/**
	 * 初始化服务器资源
	 */
	private void init(){
		client = new ZkClient(server,5000,5000,new BytesPushThroughSerializer());
		cleanExector = Executors.newFixedThreadPool(10);
		try{
			client.createPersistent(root,true);
		}catch (ZkNodeExistsException e){
			//ignore;
		}
	}
	
	/**
	 * 释放服务器资源
	 */
	private void freeResource(){
		cleanExector.shutdown();
		try{
			cleanExector.awaitTermination(2, TimeUnit.SECONDS);
		}catch(InterruptedException e){
			e.printStackTrace();
		}finally{
			cleanExector = null;
		}
	
		if (client!=null){
			client.close();
			client = null;
		}
	}
	
	/**
	 * 	监测当前服务器 是否 正在运行
	 */
	private void checkRunning() throws Exception {
		if (!running) {
			throw new Exception("请先调用start");
		}
	}
	
	public String generateId(RemoveMethod removeMethod) throws Exception {
		//只有在运行时才能生存id	
		checkRunning();
		
		final String fullNodePath = root.concat("/").concat(nodeName);
		final String ourPath = client.createPersistentSequential(fullNodePath, null);
		
		if (removeMethod.equals(RemoveMethod.IMMEDIATELY)) {
			client.delete(ourPath);
		} else if (removeMethod.equals(RemoveMethod.DELAY)) {
			cleanExector.execute(new Runnable() {
				public void run() {
					client.delete(ourPath);
				}
			});
		}
		
		//node-0000000000, node-0000000001
		return ExtractId(ourPath);
	}

	private String ExtractId(String str){
		int index = str.lastIndexOf(nodeName);
		if (index >= 0){
			index += nodeName.length();
			return index <= str.length() ? str.substring(index) : "";
		}
		return str;
	}
	
//	public static void main(String[] args) {
//		String nodeName = "ID";
//		String str = "ID00000001";
//		int index = str.lastIndexOf(nodeName);
//		if (index >= 0){
//			index += nodeName.length();
//			 String id = index <= str.length() ? str.substring(index) : "";
//			 System.out.println(id);
//		}
//	}
}
