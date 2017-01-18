package com.github.luohaha.paxos.packet;

public class LearnResponse {
	private int id;
	private int instance;
	private String value;
	public LearnResponse(int id, int instance, String value) {
		super();
		this.id = id;
		this.instance = instance;
		this.value = value;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getInstance() {
		return instance;
	}
	public void setInstance(int instance) {
		this.instance = instance;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
