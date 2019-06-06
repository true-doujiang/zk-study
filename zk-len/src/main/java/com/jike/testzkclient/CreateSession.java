package com.jike.testzkclient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class CreateSession {
	
/**
	ZkClient是Github上一个开源的ZooKeeper客户端。ZkClient在ZooKeeper原生API接口之上进行了包装，是一个更加易用的ZooKeeper客户端。
	同时，ZkClient在内部实现了诸如Session超时重连、Watcher反复注册等功能。
*/
	public static void main(String[] args) {
												//session失效时间	连接超时时间   节点数据的序列化器
		ZkClient zc = new ZkClient("192.168.254.140:2181",10000,10000,new SerializableSerializer());
		System.out.println(zc);
		System.out.println("conneted ok!");
	}
	
}
