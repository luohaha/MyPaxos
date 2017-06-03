package com.github.luohaha.paxos.packet;

import java.io.Serializable;

public class AcceptPacket implements Serializable {
	private int id;
	private int instance;
	private int ballot;
	private Value value;
	public AcceptPacket(int id, int instance, int ballot, Value value) {
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
	public Value getValue() {
		return value;
	}
	public void setValue(Value value) {
		this.value = value;
	}
	
}
