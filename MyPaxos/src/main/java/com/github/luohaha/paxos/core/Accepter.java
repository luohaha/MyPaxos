package com.github.luohaha.paxos.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.github.luohaha.paxos.packet.AcceptPacket;
import com.github.luohaha.paxos.packet.AcceptResponsePacket;
import com.github.luohaha.paxos.packet.Packet;
import com.github.luohaha.paxos.packet.PacketBean;
import com.github.luohaha.paxos.packet.PreparePacket;
import com.github.luohaha.paxos.packet.PrepareResponsePacket;
import com.github.luohaha.paxos.packet.Value;
import com.github.luohaha.paxos.utils.ConfReader;
import com.github.luohaha.paxos.utils.FileUtils;
import com.github.luohaha.paxos.utils.client.CommClient;
import com.github.luohaha.paxos.utils.client.CommClientImpl;
import com.github.luohaha.paxos.utils.serializable.ObjectSerialize;
import com.github.luohaha.paxos.utils.serializable.ObjectSerializeImpl;
import com.github.luohaha.paxos.utils.server.CommServer;
import com.github.luohaha.paxos.utils.server.CommServerImpl;
import com.github.luohaha.paxos.utils.server.NonBlockServerImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Accepter {
	static class Instance {
		// current ballot number
		private int ballot;
		// accepted value
		private Value value;
		// accepted value's ballot
		private int acceptedBallot;

		public Instance(int ballot, Value value, int acceptedBallot) {
			super();
			this.ballot = ballot;
			this.value = value;
			this.acceptedBallot = acceptedBallot;
		}

		public void setValue(Value value) {
			this.value = value;
		}
	}

	// accepter's state, contain each instances
	private Map<Integer, Instance> instanceState = new HashMap<>();
	// accepted value
	private Map<Integer, Value> acceptedValue = new HashMap<>();
	// accepter's id
	private transient int id;
	// proposers
	private transient List<InfoObject> proposers;
	// my conf
	private transient InfoObject my;
	// 保存最近一次成功提交的instance，用于优化
	private volatile int lastInstanceId = 0;
	// 配置文件
	private ConfObject confObject;
	// 组id
	private int groupId;

	private Gson gson = new Gson();
	
	private ObjectSerialize objectSerialize = new ObjectSerializeImpl();
	
	private Logger logger = Logger.getLogger("MyPaxos");
	//客户端
	private CommClient client;

	// 消息队列，保存packetbean
	private BlockingQueue<PacketBean> msgQueue = new LinkedBlockingQueue<>();

	public Accepter(int id, List<InfoObject> proposers, InfoObject my, ConfObject confObject, int groupId, CommClient client) {
		this.id = id;
		this.proposers = proposers;
		this.my = my;
		this.confObject = confObject;
		this.groupId = groupId;
		this.client = client;
		instanceRecover();
		new Thread(() -> {
			while (true) {
				try {
					PacketBean msg = msgQueue.take();
					recvPacket(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	/**
	 * 向消息队列中插入packetbean
	 * @param bean
	 * @throws InterruptedException
	 */
	public void sendPacket(PacketBean bean) throws InterruptedException {
		this.msgQueue.put(bean);
	}

	/**
	 * 处理接收到的packetbean
	 * 
	 * @param bean
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void recvPacket(PacketBean bean) throws UnknownHostException, IOException {
		switch (bean.getType()) {
		case "PreparePacket":
			//PreparePacket preparePacket = gson.fromJson(bean.getData(), PreparePacket.class);
			PreparePacket preparePacket = (PreparePacket) bean.getData();
			onPrepare(preparePacket.getPeerId(), preparePacket.getInstance(), preparePacket.getBallot());
			break;
		case "AcceptPacket":
			//AcceptPacket acceptPacket = gson.fromJson(bean.getData(), AcceptPacket.class);
			AcceptPacket acceptPacket = (AcceptPacket) bean.getData();
			onAccept(acceptPacket.getId(), acceptPacket.getInstance(), acceptPacket.getBallot(),
					acceptPacket.getValue());
			break;
		default:
			System.out.println("unknown type!!!");
			break;
		}
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
	private void prepareResponse(int peerId, int id, int instance, boolean ok, int ab, Value av)
			throws UnknownHostException, IOException {
		PacketBean bean = new PacketBean("PrepareResponsePacket", 
				new PrepareResponsePacket(id, instance, ok, ab, av));
		InfoObject peer = getSpecInfoObect(peerId);
		this.client.sendTo(peer.getHost(), peer.getPort(), 
				this.objectSerialize.objectToObjectArray(new Packet(bean, groupId, WorkerType.PROPOSER)));
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
	public void onAccept(int peerId, int instance, int ballot, Value value) throws UnknownHostException, IOException {
		this.logger.info("[onaccept]" + peerId + " " + instance + " " + ballot + " " + value);
		if (!this.instanceState.containsKey(instance)) {
			acceptResponse(peerId, id, instance, false);
		} else {
			Instance current = this.instanceState.get(instance);
			if (ballot == current.ballot) {
				current.acceptedBallot = ballot;
				current.value = value;
				// 成功
				this.logger.info("[onaccept success]");
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
		this.logger.info("[onaccept end]");
	}

	private void acceptResponse(int peerId, int id, int instance, boolean ok) throws UnknownHostException, IOException {
		InfoObject infoObject = getSpecInfoObect(peerId);
		PacketBean bean = new PacketBean("AcceptResponsePacket", new AcceptResponsePacket(id, instance, ok));
		this.client.sendTo(infoObject.getHost(), infoObject.getPort(),
				this.objectSerialize.objectToObjectArray(new Packet(bean, groupId, WorkerType.PROPOSER)));
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
			FileWriter fileWriter = new FileWriter(getInstanceFileAddr());
			fileWriter.write(gson.toJson(this.instanceState));
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
		String data = FileUtils.readFromFile(getInstanceFileAddr());
		if (data == null || data.length() == 0) {
			File file = new File(getInstanceFileAddr());
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
		this.instanceState.putAll(gson.fromJson(data, new TypeToken<Map<Integer, Instance>>() {
		}.getType()));
		this.instanceState.forEach((key, value) -> {
			if (value.value != null)
				this.acceptedValue.put(key, value.value);
		});
	}
	
	/**
	 * 获取instance持久化的文件位置
	 * @return
	 */
	private String getInstanceFileAddr() {
		return this.confObject.getDataDir() + "accepter-" + this.groupId + "-" + this.id + ".json";
	}

	public Map<Integer, Value> getAcceptedValue() {
		return acceptedValue;
	}

	public Map<Integer, Instance> getInstanceState() {
		return instanceState;
	}

	public int getId() {
		return id;
	}

	public ConfObject getConfObject() {
		return confObject;
	}

	public int getGroupId() {
		return groupId;
	}

}
