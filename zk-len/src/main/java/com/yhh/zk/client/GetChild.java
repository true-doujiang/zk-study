package com.yhh.zk.client;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class GetChild {

	public static void main(String[] args) {
		ZkClient zc = new ZkClient("192.168.254.140:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		
		List<String> cList = zc.getChildren("/jike5");
		
		System.out.println(cList.toString());
		
	}
	
}
