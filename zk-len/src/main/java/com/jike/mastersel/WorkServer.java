package com.jike.mastersel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

/**
	释放MASTER权利的几种情况
	1、MASTER节点主动释放MASTER权利  stop()
	2、MASTER节点宕机   此时session就会失效    zookeeper检测到就会把MASTER节点（临时节点）删掉
	3、网络抖动   当前MASTER节点以为网络不好 暂时与zookeeper集群失去连接    zookeeper就会误认为MASTER节点已经宕机，
			 就会删除MASTER节点，导致包括原来MASTER节点在内的所有WorkServer一起争端MASTER权利。
		俩中情况① 再次选出来的MASTER同上一次的MASTER是同一个，这种情况最好
			  ② 再次选出来的MASTER同上一次的MASTER不是同一个，就会导致数据迁移  
 */
public class WorkServer {

	//当前服务器状态
	private volatile boolean running = false;

	private ZkClient zkClient;

	//MASTER节点对应的zookeeper中路径
	private static final String MASTER_PATH = "/master";

	//监听MASTER节点的数据变化
	private IZkDataListener dataListener;

	//记录当前服务器的基本信息
	private RunningData serverData;
	//记录集群中MASTER务器的基本信息
	private RunningData masterData;
	
	//调度型 的线程池
	private ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);
	private int delayTime = 5;

	//构造器
	public WorkServer(RunningData rd) {
		this.serverData = rd;
		this.dataListener = new IZkDataListener() {

			public void handleDataDeleted(String dataPath) throws Exception {
				System.err.println("MASTER节点列表发生变化..."); 
				//检测到master节点被删除的时候立即争夺MASTER权利
				//不启用网络抖动优化
				//takeMaster();
				
				//启用网络抖动优化
				if (masterData!=null && masterData.getName().equals(serverData.getName())){
					takeMaster();
				}else{
					delayExector.schedule(new Runnable(){
						public void run(){
							takeMaster();
						}
					}, delayTime, TimeUnit.SECONDS);
				}
			}

			public void handleDataChange(String dataPath, Object data) throws Exception {

			}
		};
	}

	//启动服务器
	public void start() throws Exception {
		if (running) {
			throw new Exception("WorkServer has startup...");
		}
		running = true;
		
		//订阅MASTER节点的删除事件
		zkClient.subscribeDataChanges(MASTER_PATH, dataListener);
		
		//争夺MASTER权利
		takeMaster();
	}

	//关闭服务器
	public void stop() throws Exception {
		if (!running) {
			throw new Exception("WorkServer has stoped");
		}
		running = false;
		
		delayExector.shutdown();
		
		//取消MASTER节点的事件订阅
		zkClient.unsubscribeDataChanges(MASTER_PATH, dataListener);

		releaseMaster();
	}

	//争抢MASTER权利
	private void takeMaster() {
		if (!running){
			return;
		}
		try {
			//创建MASTER节点						一定是临时节点
			zkClient.create(MASTER_PATH, serverData, CreateMode.EPHEMERAL);
			masterData = serverData;
			System.out.println(serverData.getName()+" is master");
			
			//这里纯粹为了演示争夺MASTER权利用的    5s中后释放MASTER权利
			delayExector.schedule(new Runnable() {			
				public void run() {
					if (checkMaster()){
						releaseMaster();
					}
				}
			}, 5, TimeUnit.SECONDS);
			
		} catch (ZkNodeExistsException e) {//MASTER节点已经创建过了的异常
			System.err.println("创建MASTER节点已存在异常，说明已经存在MASTER了..."); 
			RunningData masterRunningData = zkClient.readData(MASTER_PATH, true);
			//如果没有读到数据说明MASTER节点已经宕机了
			if (masterRunningData == null) {
				System.err.println("MASTER节点已经宕机了 争抢MASTER权利..."); 
				takeMaster();
			} else {
				masterData = masterRunningData;
				System.err.println(serverData.getName() + " 同步MASTER信息..."); 
			}
		} catch (Exception e) {
			// ignore;
		}
	}

	//释放MASTER权利
	private void releaseMaster() {
		if (checkMaster()) {
			zkClient.delete(MASTER_PATH);
		}
	}
	
	//监测自己是不是MASTER
	private boolean checkMaster() {
		try {
			RunningData eventData = zkClient.readData(MASTER_PATH);
			masterData = eventData;
			if (masterData.getName().equals(serverData.getName())) {
				return true;
			} 
			return false;
		} catch (ZkNoNodeException e) {
			//如果MASTER节点不存在抛ZkNoNodeException
			return false;
		} catch (ZkInterruptedException e) {
			//终端异常   重试一次
			boolean b = checkMaster();
			return b;
		} catch (ZkException e) {
			return false;
		}
	}

	
	public ZkClient getZkClient() {
		return zkClient;
	}

	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}
}
