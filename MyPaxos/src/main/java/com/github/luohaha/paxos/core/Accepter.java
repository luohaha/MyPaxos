package com.github.luohaha.paxos.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.luohaha.paxos.packet.AcceptPacket;
import com.github.luohaha.paxos.packet.AcceptResponsePacket;
import com.github.luohaha.paxos.packet.PacketBean;
import com.github.luohaha.paxos.packet.PreparePacket;
import com.github.luohaha.paxos.packet.PrepareResponsePacket;
import com.github.luohaha.paxos.utils.CommClient;
import com.github.luohaha.paxos.utils.CommClientImpl;
import com.github.luohaha.paxos.utils.CommServer;
import com.github.luohaha.paxos.utils.CommServerImpl;
import com.github.luohaha.paxos.utils.ConfReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Accepter {
	static class Instance {
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

		public void setValue(Object value) {
			this.value = value;
		}
	}

	// accepter's state, contain each instances
	private Map<Integer, Instance> instanceState = new HashMap<>();
	// accepted value
	private Map<Integer, Object> acceptedValue = new HashMap<>();
	// accepter's id
	private transient int id;
	// proposers
	private transient List<InfoObject> proposers;
	// my conf
	private transient InfoObject my;
	// 保存最近一次成功提交的instance，用于优化
	private int lastInstanceId = 0;
	// 配置文件
	private ConfObject confObject;

	public Accepter(int id, List<InfoObject> proposers, InfoObject my, ConfObject confObject) {
		this.id = id;
		this.proposers = proposers;
		this.my = my;
		this.confObject = confObject;
		instanceRecover();
	}

	/**
	 * start this node
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void start() {
		new Thread(() -> {
			try {
				CommServer server = new CommServerImpl(my.getPort());
				Gson gson = new Gson();
				System.out.println("accepter-" + my.getId() + " start...");
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
						onAccept(acceptPacket.getId(), acceptPacket.getInstance(), acceptPacket.getBallot(),
								acceptPacket.getValue());
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
	 * 
	 * @param instance
	 *            current instance
	 * @param ballot
	 *            prepare ballot
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public void onPrepare(int peerId, int instance, int ballot) throws UnknownHostException, IOException {
		if (!instanceState.containsKey(instance)) {
			instanceState.put(instance, new Instance(ballot, null, 0));
			// 持久化到磁盘
			instancePersistence();
			prepareResponse(peerId, id, instance, true, 0, null);
		} else {
			Instance current = instanceState.get(instance);
			if (ballot > current.ballot) {
				current.ballot = ballot;
				// 持久化到磁盘
				instancePersistence();
				prepareResponse(peerId, id, instance, true, current.acceptedBallot, current.value);
			} else {
				prepareResponse(peerId, id, instance, false, current.ballot, null);
			}
		}
	}

	/**
	 * 
	 * @param id
	 *            accepter's id
	 * @param ok
	 *            ok or reject
	 * @param ab
	 *            accepted ballot
	 * @param av
	 *            accepted value
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private void prepareResponse(int peerId, int id, int instance, boolean ok, int ab, Object av)
			throws UnknownHostException, IOException {
		CommClient client = new CommClientImpl();
		Gson gson = new Gson();
		PacketBean bean = new PacketBean("PrepareResponsePacket",
				gson.toJson(new PrepareResponsePacket(id, instance, ok, ab, av)));
		InfoObject peer = getSpecInfoObect(peerId);
		client.sendTo(peer.getHost(), peer.getPort(), gson.toJson(bean).getBytes());
	}

	/**
	 * handle accept from proposer
	 * 
	 * @param instance
	 *            current instance
	 * @param ballot
	 *            accept ballot
	 * @param value
	 *            accept value
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
				// 成功
				this.acceptedValue.put(instance, value);
				if (!this.instanceState.containsKey(instance + 1)) {
					// multi-paxos 中的优化，省去了连续成功后的prepare阶段
					this.instanceState.put(instance + 1, new Instance(1, null, 0));
				}
				// 保存最后一次成功的instance的位置，用于proposer直接从这里开始执行
				this.lastInstanceId = instance;
				// 持久化到磁盘
				instancePersistence();
				acceptResponse(peerId, id, instance, true);
			} else {
				acceptResponse(peerId, id, instance, false);
			}
		}
	}

	private void acceptResponse(int peerId, int id, int instance, boolean ok) throws UnknownHostException, IOException {
		CommClient client = new CommClientImpl();
		Gson gson = new Gson();
		InfoObject infoObject = getSpecInfoObect(peerId);
		PacketBean bean = new PacketBean("AcceptResponsePacket",
				gson.toJson(new AcceptResponsePacket(id, instance, ok)));
		client.sendTo(infoObject.getHost(), infoObject.getPort(), new Gson().toJson(gson.toJsonTree(bean)).getBytes());
	}

	/**
	 * proposer从这获取最近的instance的id
	 * 
	 * @return
	 */
	public int getLastInstanceId() {
		return lastInstanceId;
	}

	/**
	 * 获取特定的info
	 * 
	 * @param key
	 * @return
	 */
	private InfoObject getSpecInfoObect(int key) {
		for (InfoObject each : this.proposers) {
			if (key == each.getId()) {
				return each;
			}
		}
		return null;
	}

	/**
	 * 在磁盘上存储instance
	 */
	private void instancePersistence() {
		if (!this.confObject.isEnableDataPersistence())
			return;
		try {
			FileWriter fileWriter = new FileWriter(this.confObject.getDataDir() + "accepter-" + this.id + ".json");
			fileWriter.write(new Gson().toJson(this.instanceState));
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * instance恢复
	 */
	private void instanceRecover() {
		if (!this.confObject.isEnableDataPersistence())
			return;
		String data = ConfReader.readFile(this.confObject.getDataDir() + "accepter-" + this.id + ".json");
		if (data == null || data.length() == 0) {
			File file = new File(this.confObject.getDataDir() + "accepter-" + this.id + ".json");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return;
		}
		this.instanceState.putAll(new Gson().fromJson(data, new TypeToken<Map<Integer, Instance>>() {}.getType()));
		this.instanceState.forEach((key, value) -> {
			if (value.value != null)
				this.acceptedValue.put(key, value.value);
		});
	}

	public Map<Integer, Object> getAcceptedValue() {
		return acceptedValue;
	}

	public Map<Integer, Instance> getInstanceState() {
		return instanceState;
	}

}
