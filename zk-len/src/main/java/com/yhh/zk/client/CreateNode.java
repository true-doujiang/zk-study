package com.yhh.zk.client;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;

public class CreateNode {

	public static void main(String[] args) {
		ZkClient zc = new ZkClient("39.106.63.228:2183",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		
		
		User u = new User();
		u.setId(1);
		u.setName("test");
		String path = zc.create("/jike1", u, CreateMode.PERSISTENT);
		//zc.createEphemeral(path, data, acl);
		System.out.println("created path:"+path);
	}
	
}
