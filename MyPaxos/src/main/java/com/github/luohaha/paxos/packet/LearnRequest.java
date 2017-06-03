package com.github.luohaha.paxos.packet;

import java.io.Serializable;

public class LearnRequest implements Serializable {
	private int id;
	private int instance;
	public LearnRequest(int id, int instance) {
		super();
		this.id = id;
		this.instance = instance;
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
	
}
