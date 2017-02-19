package com.github.luohaha.paxos.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiDevice.Info;

import com.github.luohaha.paxos.core.Accepter;
import com.github.luohaha.paxos.core.ConfObject;
import com.github.luohaha.paxos.core.InfoObject;
import com.github.luohaha.paxos.core.Learner;
import com.github.luohaha.paxos.core.PaxosCallback;
import com.github.luohaha.paxos.core.Proposer;
import com.github.luohaha.paxos.packet.Packet;
import com.github.luohaha.paxos.utils.CommServer;
import com.github.luohaha.paxos.utils.CommServerImpl;
import com.github.luohaha.paxos.utils.ConfReader;
import com.github.luohaha.paxos.utils.NonBlockServerImpl;
import com.google.gson.Gson;

public class MyPaxos {

	/**
	 * 全局配置文件信息
	 */
	private ConfObject confObject;
	
	/**
	 * 本节点的信息
	 */
	private InfoObject infoObject;

	/**
	 * 配置文件所在的位置
	 */
	private String confFile;
	
	private Map<Integer, PaxosCallback> groupidToCallback = new HashMap<>();
	
	private Map<Integer, Proposer> groupidToProposer = new HashMap<>();
	
	private Map<Integer, Accepter> groupidToAccepter = new HashMap<>();
	
	private Map<Integer, Learner> groupidToLearner = new HashMap<>();
	
	private Gson gson = new Gson();

	public MyPaxos(String confFile) {
		super();
		this.confFile = confFile;
		this.confObject = gson.fromJson(ConfReader.readFile(this.confFile), ConfObject.class);
		this.infoObject = getMy(this.confObject.getNodes());
	}
	
	/**
	 * 
	 * @param id
	 * @param executor
	 */
	public void setGroupId(int groupId, PaxosCallback executor) {
		Accepter accepter = new Accepter(infoObject.getId(), confObject.getNodes(), infoObject, confObject, groupId);
		Proposer proposer = new Proposer(infoObject.getId(), confObject.getNodes(), infoObject, confObject.getTimeout(), accepter, groupId);
		Learner learner = new Learner(infoObject.getId(), confObject.getNodes(), infoObject, confObject, accepter, executor, groupId);
		this.groupidToCallback.put(groupId, executor);
		this.groupidToAccepter.put(groupId, accepter);
		this.groupidToProposer.put(groupId, proposer);
		this.groupidToLearner.put(groupId, learner);
	}

	/**
	 * 获得我的accepter或者proposer信息
	 * 
	 * @param accepters
	 * @return
	 */
	private InfoObject getMy(List<InfoObject> infoObjects) {
		for (InfoObject each : infoObjects) {
			if (each.getId() == confObject.getMyid()) {
				return each;
			}
		}
		return null;
	}

	/**
	 * 启动paxos服务器
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void start() throws IOException, InterruptedException {
		
		// 启动paxos服务器
		CommServer server = new NonBlockServerImpl(this.infoObject.getPort());
		System.out.println("paxos server-" + confObject.getMyid() + " start...");
		while (true) {
			byte[] data = server.recvFrom();
			Packet packet = gson.fromJson(new String(data), Packet.class);
			int groupId = packet.getGroupId();
			Accepter accepter = this.groupidToAccepter.get(groupId);
			Proposer proposer = this.groupidToProposer.get(groupId);
			Learner learner = this.groupidToLearner.get(groupId);
			if (accepter == null || proposer == null || learner == null) {
				return;
			}
			switch (packet.getWorkerType()) {
			case ACCEPTER:
				accepter.sendPacket(packet.getPacketBean());
				break;
			case PROPOSER:
				proposer.sendPacket(packet.getPacketBean());
				break;
			case LEARNER:
				learner.sendPacket(packet.getPacketBean());
				break;
			case SERVER:
				proposer.sendPacket(packet.getPacketBean());
				break;
			default:
				break;
			}
		}
	}
}
