package com.jike.testzkclient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class NodeDel {

	public static void main(String[] args) {
		ZkClient zc = new ZkClient("192.168.254.140:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		
		boolean e = zc.delete("/jike5");
		boolean e2 = zc.deleteRecursive("/jike5");//有子节点用我
		System.out.println(e);
		System.out.println(e2);
	}
	
}
