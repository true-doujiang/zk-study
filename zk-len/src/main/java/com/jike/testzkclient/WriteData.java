package com.jike.testzkclient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class WriteData {

	public static void main(String[] args) {
		ZkClient zc = new ZkClient("192.168.254.140:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		
		User u = new User();
		u.setId(2);
		u.setName("test2");
		//zc.writeData("/jike5", u);
		zc.writeData("/jike5", u, 1);
	}
	
}
