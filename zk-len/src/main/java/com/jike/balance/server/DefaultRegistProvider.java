package com.jike.balance.server;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

public class DefaultRegistProvider implements RegistProvider {

	/**
	 * 注册   在zookeeper中创建一个临时节点并写入自己的基本信息
	 */
	public void regist(Object context) throws Exception {
		// 1:path  2:zkClient   3:serverData   这3个信息封装到ZooKeeperRegistContext中

		ZooKeeperRegistContext registContext = (ZooKeeperRegistContext) context;
		String path = registContext.getPath();
		ZkClient zc = registContext.getZkClient();
		Object data = registContext.getData();
		try {
			zc.createEphemeral(path, data);
		} catch (ZkNoNodeException e) {
			//创建父节点
			String parentDir = path.substring(0, path.lastIndexOf('/'));
			zc.createPersistent(parentDir, true);
			regist(registContext);
		}
	}

	/**
	 * 次方本例中不关心
	 */
	public void unRegist(Object context) throws Exception {
		return;
	}

}
