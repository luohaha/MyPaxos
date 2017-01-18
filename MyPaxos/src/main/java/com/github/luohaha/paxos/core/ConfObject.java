package com.github.luohaha.paxos.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfObject {
	private List<InfoObject> nodes = new ArrayList<>();
	private int myid;
	private int timeout;
	private int learningInterval;
	private String dataDir;
	private boolean enableDataPersistence;

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

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	public boolean isEnableDataPersistence() {
		return enableDataPersistence;
	}

	public void setEnableDataPersistence(boolean enableDataPersistence) {
		this.enableDataPersistence = enableDataPersistence;
	}

	public int getLearningInterval() {
		return learningInterval;
	}

	public void setLearningInterval(int learningInterval) {
		this.learningInterval = learningInterval;
	}
	
}
