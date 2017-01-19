package com.github.luohaha.paxos.kvTest;

public class MsgBean {
	
	private String type;
	private String key;
	private String value;
	public MsgBean(String type, String key, String value) {
		super();
		this.type = type;
		this.key = key;
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
