package com.github.luohaha.paxos.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfObject {
	private List<InfoObject> nodes = new ArrayList<>();
	private int myid;
	private int timeout;

	public ConfObject() {
	}

	public List<InfoObject> getNodes() {
		return nodes;
	}

	public void setNodes(List<InfoObject> nodes) {
		this.nodes = nodes;
	}

	public int getMyid() {
		return myid;
	}

	public void setMyid(int myid) {
		this.myid = myid;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
}
