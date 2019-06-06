package com.jike.testzkclient;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

/**
 *	事件订阅  ：
 *		1、订阅该节点的子节点列表变化（该节点变化也会收到事件）
 *		2、订阅该节点数据变化
 */
public class SubscribeChildChanges {
	
	public static void main(String[] args) throws InterruptedException {
		ZkClient zc = new ZkClient("192.168.254.140:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		
		// jike20节点可以先不存在   然后在Linux下创建
		zc.subscribeChildChanges("/jike20", new ZkChildListener());
		Thread.sleep(Integer.MAX_VALUE);
	}
	
	private static class ZkChildListener implements IZkChildListener{

		public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
			System.out.println(parentPath);
			System.out.println(currentChilds.toString());
		}
	}


	
}
