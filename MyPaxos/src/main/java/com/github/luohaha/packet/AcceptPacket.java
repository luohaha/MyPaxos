package com.github.luohaha.packet;

public class AcceptPacket {
	private int id;
	private int instance;
	private int ballot;
	private Object value;
	public AcceptPacket(int id, int instance, int ballot, Object value) {
		super();
		this.id = id;
		this.instance = instance;
		this.ballot = ballot;
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
	public int getBallot() {
		return ballot;
	}
	public void setBallot(int ballot) {
		this.ballot = ballot;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
}
