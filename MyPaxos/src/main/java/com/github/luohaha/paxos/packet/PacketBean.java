package com.github.luohaha.paxos.packet;

import java.io.Serializable;

public class PacketBean implements Serializable {
	private String type;
	private Object data;
	public PacketBean(String type, Object data) {
		super();
		this.type = type;
		this.data = data;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
}
