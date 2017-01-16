package com.github.luohaha.paxos.core;

public class InfoObject {
	private int id;
	private String host;
	private int port;
	public InfoObject(int id, String host, int port) {
		super();
		this.id = id;
		this.host = host;
		this.port = port;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
}
