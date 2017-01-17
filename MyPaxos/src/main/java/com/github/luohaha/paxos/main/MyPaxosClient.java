package com.github.luohaha.paxos.main;

import java.io.IOException;
import java.net.UnknownHostException;

import com.github.luohaha.paxos.utils.CommClient;
import com.github.luohaha.paxos.utils.CommClientImpl;

public class MyPaxosClient {
	// 要发往的proposer的host地址
	private String host;
	// proposer的port
	private int port;
	// comm client
	private CommClient commClient;
	
	public MyPaxosClient(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		this.commClient = new CommClientImpl();
	}
	
	public void submit(String value) throws UnknownHostException, IOException {
		this.commClient.sendTo(this.host, this.port, value.getBytes());
	}
}
