package com.github.luohaha.paxos.core;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import com.github.luohaha.packet.AcceptPacket;
import com.github.luohaha.packet.AcceptResponsePacket;
import com.github.luohaha.packet.PacketBean;
import com.github.luohaha.packet.PreparePacket;
import com.github.luohaha.packet.PrepareResponsePacket;
import com.github.luohaha.paxos.utils.CommClient;
import com.github.luohaha.paxos.utils.CommClientImpl;
import com.github.luohaha.paxos.utils.CommServer;
import com.github.luohaha.paxos.utils.CommServerImpl;
import com.google.gson.Gson;

public class Accepter {
	class Instance {
		// current ballot number
		private int ballot;
		// accepted value
		private Object value;
		// accepted value's ballot
		private int acceptedBallot;
		public Instance(int ballot, Object value, int acceptedBallot) {
			super();
			this.ballot = ballot;
			this.value = value;
			this.acceptedBallot = acceptedBallot;
		}
	}
	
	// accepter's state, contain each instances
	private Map<Integer, Instance> instanceState = new HashMap<>();
	// accepted value
	private Map<Integer, Object> acceptedValue = new HashMap<>();
	// accepter's id
	private int id;
	// proposers
	private List<InfoObject> proposers;
	//my conf
	private InfoObject my;
	
	public Accepter(int id, List<InfoObject> proposers, InfoObject my) {
		this.id = id;
		this.proposers = proposers;
		this.my = my;
	}
	
	/**
	 * start this node
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void start() {
		new Thread(() -> {
			try {
				CommServer server = new CommServerImpl(my.getPort());
				Gson gson = new Gson();
				System.out.println("accepter-" + my.getId() + " start!");
				while (true) {
					byte[] data = server.recvFrom();
					PacketBean bean = gson.fromJson(new String(data), PacketBean.class);
					switch (bean.getType()) {
					case "PreparePacket":
						PreparePacket preparePacket = gson.fromJson(bean.getData(), PreparePacket.class);
						onPrepare(preparePacket.getPeerId(), preparePacket.getInstance(), preparePacket.getBallot());
						break;
					case "AcceptPacket":
						AcceptPacket acceptPacket = gson.fromJson(bean.getData(), AcceptPacket.class);
						onAccept(acceptPacket.getId(), acceptPacket.getInstance(), acceptPacket.getBallot(), acceptPacket.getValue());
						break;
					default:
						System.out.println("unknown type!!!");
						break;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * handle prepare from proposer
	 * @param instance
	 * current instance
	 * @param ballot
	 * prepare ballot
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public void onPrepare(int peerId, int instance, int ballot) throws UnknownHostException, IOException {
		if (!instanceState.containsKey(instance)) {
			instanceState.put(instance, new Instance(ballot, null, 0));
			prepareResponse(peerId, id, instance, true, 0, null);
		} else {
			Instance current = instanceState.get(instance);
			if (ballot > current.ballot) {
				current.ballot = ballot;
				prepareResponse(peerId, id, instance, true, current.acceptedBallot, current.value);
			} else {
				prepareResponse(peerId, id, instance, false, current.ballot, null);
			}
		}
	}

	/**
	 * 
	 * @param id
	 * accepter's id
	 * @param ok
	 * ok or reject
	 * @param ab
	 * accepted ballot
	 * @param av
	 * accepted value
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	private void prepareResponse(int peerId, int id, int instance, boolean ok, int ab, Object av) throws UnknownHostException, IOException {
		CommClient client = new CommClientImpl();
		Gson gson = new Gson();
		PacketBean bean = new PacketBean("PrepareResponsePacket", gson.toJson(new PrepareResponsePacket(id, instance, ok, ab, av)));
		InfoObject peer = getSpecInfoObect(peerId);
		client.sendTo(peer.getHost(), peer.getPort(), 
				gson.toJson(bean).getBytes());
	}
	
	/**
	 * handle accept from proposer
	 * @param instance
	 * current instance
	 * @param ballot
	 * accept ballot 
	 * @param value
	 * accept value
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public void onAccept(int peerId, int instance, int ballot, Object value) throws UnknownHostException, IOException {
		if (!this.instanceState.containsKey(instance)) {
			acceptResponse(peerId, id, instance, false);
		} else {
			Instance current = this.instanceState.get(instance);
			if (ballot == current.ballot) {
				current.acceptedBallot = ballot;
				current.value = value;
				acceptResponse(peerId, id, instance, true);
				this.acceptedValue.put(instance, value);
			} else {
				acceptResponse(peerId, id, instance, false);
			}
		}
	}
	
	private void acceptResponse(int peerId, int id, int instance, boolean ok) throws UnknownHostException, IOException {
		CommClient client = new CommClientImpl();
		Gson gson = new Gson();
		InfoObject infoObject = getSpecInfoObect(peerId);
		PacketBean bean = new PacketBean("AcceptResponsePacket", gson.toJson(new AcceptResponsePacket(id, instance, ok)));
		client.sendTo(infoObject.getHost(), infoObject.getPort(), 
				new Gson().toJson(gson.toJsonTree(bean)).getBytes());
	}
	
	private InfoObject getSpecInfoObect(int key) {
		for (InfoObject each : this.proposers) {
			if (key == each.getId()) {
				return each;
			}
		}
		return null;
	}
}
