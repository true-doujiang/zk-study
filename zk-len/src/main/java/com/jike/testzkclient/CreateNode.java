package com.jike.testzkclient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;

public class CreateNode {

	public static void main(String[] args) {
		ZkClient zc = new ZkClient("192.168.254.140:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		
		
		User u = new User();
		u.setId(1);
		u.setName("test");
		String path = zc.create("/jike5", u, CreateMode.PERSISTENT);
		//zc.createEphemeral(path, data, acl);
		System.out.println("created path:"+path);
	}
	
}
