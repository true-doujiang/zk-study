package com.jike.testzkclient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class NodeExists {

	public static void main(String[] args) {
		ZkClient zc = new ZkClient("192.168.254.140:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		
		boolean e = zc.exists("/jike5");
		
		System.out.println(e);
		
	}
	
}
