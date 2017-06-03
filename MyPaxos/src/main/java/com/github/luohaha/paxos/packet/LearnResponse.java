package com.github.luohaha.paxos.packet;

import java.io.Serializable;

public class LearnResponse implements Serializable {
	private int id;
	private int instance;
	private Value value;
	public LearnResponse(int id, int instance, Value value) {
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
	public Value getValue() {
		return value;
	}
	public void setValue(Value value) {
		this.value = value;
	}
	
	
}
