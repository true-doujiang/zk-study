package com.jike.nameservice;

import com.jike.nameservice.IdMaker.RemoveMethod;

public class TestIdMaker {

	/**
	 * 	zookeeper关闭再开启也是接着原来的序号   不是从0再开始的
	 */
	public static void main(String[] args) throws Exception {
		
		IdMaker idMaker = new IdMaker("192.168.254.138:2181", "/NameService/IdGen", "ID");
		idMaker.start();

		try {
			for (int i = 0; i < 10; i++) {
				String id = idMaker.generateId(RemoveMethod.DELAY);
				System.out.println(id);
			}
		} finally {
			idMaker.stop();
		}
	}

}
