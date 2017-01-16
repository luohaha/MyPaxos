package com.github.luohaha.paxos.packet;

public class PrepareResponsePacket {
	private int id;
	private int instance;
	private boolean ok;
	private int ab;
	private Object av;
	public PrepareResponsePacket(int id, int instance, boolean ok, int ab, Object av) {
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
	public Object getAv() {
		return av;
	}
	public void setAv(Object av) {
		this.av = av;
	}
	
}
