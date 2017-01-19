package com.github.luohaha.paxos.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice.Info;

import com.github.luohaha.paxos.core.Accepter;
import com.github.luohaha.paxos.core.ConfObject;
import com.github.luohaha.paxos.core.InfoObject;
import com.github.luohaha.paxos.core.Learner;
import com.github.luohaha.paxos.core.PaxosExecutor;
import com.github.luohaha.paxos.core.Proposer;
import com.github.luohaha.paxos.utils.CommServer;
import com.github.luohaha.paxos.utils.CommServerImpl;
import com.github.luohaha.paxos.utils.ConfReader;
import com.google.gson.Gson;

public class MyPaxos {
	
	/**
	 * 全局配置文件信息
	 */
	private ConfObject confObject;
	
	/**
	 * paxos状态执行者
	 */
	private PaxosExecutor executor;

	public MyPaxos(PaxosExecutor executor) {
		super();
		this.executor = executor;
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
		return getSpecList(1);
	}
	
	/**
	 * 获得全部的proposer信息
	 * @return
	 */
	private List<InfoObject> getProposerList() {
		return getSpecList(2);
	}
	
	/**
	 * 获得全部的learner信息
	 * @return
	 */
	private List<InfoObject> getLearnerList() {
		return getSpecList(3);
	}
	
	/**
	 * 获取特定端口偏移的队列
	 * @param delay
	 * @return
	 */
	private List<InfoObject> getSpecList(int delay) {
		List<InfoObject> list = new ArrayList<>();
		confObject.getNodes().forEach((info) -> {
			list.add(new InfoObject(info.getId(), info.getHost(), info.getPort() + delay));
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
		List<InfoObject> learners = getLearnerList();
		InfoObject myLearner = getMy(learners);
		// 启动accepter
		Accepter accepter = new Accepter(myAccepter.getId(), proposers, myAccepter, confObject);
		accepter.start();
		// 启动proposer
		Proposer proposer = new Proposer(myProposer.getId(), accepters, myProposer, this.confObject.getTimeout(), accepter);
		proposer.start();
		// 启动learner
		Learner learner = new Learner(myLearner.getId(), learners, myLearner, confObject, accepter, this.executor);
		learner.start();
		// 启动paxos服务器
		CommServer server = new CommServerImpl(getMy(this.confObject.getNodes()).getPort());
		System.out.println("paxos server-" + confObject.getMyid() + " start...");
		while (true) {
			byte[] data = server.recvFrom();
			proposer.submit(new String(data));
		}
	}
}
