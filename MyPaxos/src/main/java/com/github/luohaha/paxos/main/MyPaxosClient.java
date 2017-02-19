package com.github.luohaha.paxos.main;

import java.io.IOException;
import java.net.UnknownHostException;

import com.github.luohaha.paxos.core.WorkerType;
import com.github.luohaha.paxos.packet.Packet;
import com.github.luohaha.paxos.packet.PacketBean;
import com.github.luohaha.paxos.utils.CommClient;
import com.github.luohaha.paxos.utils.CommClientImpl;
import com.google.gson.Gson;

public class MyPaxosClient {
	// 要发往的proposer的host地址
	private String host;
	// proposer的port
	private int port;
	// comm client
	private CommClient commClient;
	
	private Gson gson = new Gson();
	
	public MyPaxosClient(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		this.commClient = new CommClientImpl();
	}
	
	public void submit(String value, int groupId) throws UnknownHostException, IOException {
		Packet packet = new Packet(new PacketBean("SubmitPacket", value), groupId, WorkerType.SERVER);
		this.commClient.sendTo(this.host, this.port, gson.toJson(packet).getBytes());
	}
}
