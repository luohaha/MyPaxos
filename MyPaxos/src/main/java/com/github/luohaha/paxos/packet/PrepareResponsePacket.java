package com.github.luohaha.paxos.packet;

import java.io.Serializable;

public class PrepareResponsePacket implements Serializable {
	private int id;
	private int instance;
	private boolean ok;
	private int ab;
	private Value av;
	public PrepareResponsePacket(int id, int instance, boolean ok, int ab, Value av) {
		super();
		this.id = id;
		this.instance = instance;
		this.ok = ok;
		this.ab = ab;
		this.av = av;
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
	public int getAb() {
		return ab;
	}
	public void setAb(int ab) {
		this.ab = ab;
	}
	public Value getAv() {
		return av;
	}
	public void setAv(Value av) {
		this.av = av;
	}
	
}
