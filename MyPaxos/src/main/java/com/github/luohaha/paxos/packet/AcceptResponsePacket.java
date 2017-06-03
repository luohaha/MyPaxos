package com.github.luohaha.paxos.packet;

import java.io.Serializable;

public class AcceptResponsePacket implements Serializable {
	private int id;
	private int instance;
	private boolean ok;
	public AcceptResponsePacket(int id, int instance, boolean ok) {
		super();
		this.id = id;
		this.instance = instance;
		this.ok = ok;
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
	public boolean isOk() {
		return ok;
	}
	public void setOk(boolean ok) {
		this.ok = ok;
	}
	
}
