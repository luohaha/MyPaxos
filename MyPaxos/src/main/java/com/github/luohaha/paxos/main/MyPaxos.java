package com.github.luohaha.paxos.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice.Info;

import com.github.luohaha.paxos.core.Accepter;
import com.github.luohaha.paxos.core.ConfObject;
import com.github.luohaha.paxos.core.InfoObject;
import com.github.luohaha.paxos.core.Proposer;
import com.github.luohaha.paxos.utils.CommServer;
import com.github.luohaha.paxos.utils.CommServerImpl;
import com.github.luohaha.paxos.utils.ConfReader;
import com.google.gson.Gson;

public class MyPaxos {
	
	private ConfObject confObject;
	
	public MyPaxos() {
		super();
	}

	/**
	 * 获得我的accepter或者proposer信息
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
	 * 获得全部的accepter信息
	 * @return
	 */
	private List<InfoObject> getAccepterList() {
		List<InfoObject> list = new ArrayList<>();
		confObject.getNodes().forEach((info) -> {
			list.add(new InfoObject(info.getId(), info.getHost(), info.getPort() + 1));
		});
		return list;
	}
	
	/**
	 * 获得全部的proposer信息
	 * @return
	 */
	private List<InfoObject> getProposerList() {
		List<InfoObject> list = new ArrayList<>();
		confObject.getNodes().forEach((info) -> {
			list.add(new InfoObject(info.getId(), info.getHost(), info.getPort() + 2));
		});
		return list;
	}
	
	public void start() throws IOException, InterruptedException {
		Gson gson = new Gson();
		this.confObject = gson.fromJson(ConfReader.readFile("./conf/conf.json"), ConfObject.class);
		List<InfoObject> accepters = getAccepterList();
		InfoObject myAccepter = getMy(accepters);
		List<InfoObject> proposers = getProposerList();
		InfoObject myProposer = getMy(proposers);
		// 启动accepter
		Accepter accepter = new Accepter(myAccepter.getId(), proposers, myAccepter);
		accepter.start();
		// 启动proposer
		Proposer proposer = new Proposer(myProposer.getId(), accepters, myProposer, this.confObject.getTimeout());
		proposer.start();
		CommServer server = new CommServerImpl(getMy(this.confObject.getNodes()).getPort());
		System.out.println("paxos node start...");
		while (true) {
			byte[] data = server.recvFrom();
			System.out.println("submit " + proposer.submit(new String(data)));
		}
	}
}
