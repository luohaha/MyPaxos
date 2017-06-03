package com.github.luohaha.paxos.packet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Value implements Serializable {
	private int id;
	private byte[] data;
	public Value(int id, byte[] data) {
		super();
		this.id = id;
		this.data = data;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	@Override
	public int hashCode() {
		return this.id;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Value other = (Value) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
