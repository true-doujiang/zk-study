package com.jike.mastersel;

import java.io.Serializable;

/**
 *  服务器基本信息
 */
public class RunningData implements Serializable {

	private static final long serialVersionUID = 4260577459043203630L;
	
	//服务器id
	private Long cid;
	//服务器名字
	private String name;
	
	public Long getCid() {
		return cid;
	}
	public void setCid(Long cid) {
		this.cid = cid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "RunningData [cid=" + cid + ", name=" + name + "]";
	}

}
