package com.github.luohaha.paxos.core;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.github.luohaha.paxos.packet.AcceptPacket;
import com.github.luohaha.paxos.packet.AcceptResponsePacket;
import com.github.luohaha.paxos.packet.Packet;
import com.github.luohaha.paxos.packet.PacketBean;
import com.github.luohaha.paxos.packet.PreparePacket;
import com.github.luohaha.paxos.packet.PrepareResponsePacket;
import com.github.luohaha.paxos.packet.Value;
import com.github.luohaha.paxos.utils.client.CommClient;
import com.github.luohaha.paxos.utils.client.CommClientImpl;
import com.github.luohaha.paxos.utils.serializable.ObjectSerialize;
import com.github.luohaha.paxos.utils.serializable.ObjectSerializeImpl;
import com.github.luohaha.paxos.utils.server.CommServer;
import com.github.luohaha.paxos.utils.server.CommServerImpl;
import com.github.luohaha.paxos.utils.server.NonBlockServerImpl;
import com.google.gson.Gson;

public class Proposer {
	enum Proposer_State {
		READY, PREPARE, ACCEPT, FINISH
	}

	class Instance {
		private int ballot;
		// a set for promise receive
		private Set<Integer> pSet;
		// value found after phase 1
		private Value value;
		// value's ballot
		private int valueBallot;
		// accept set
		private Set<Integer> acceptSet;
		// is wantvalue doneValue
		private boolean isSucc;
		// state
		private Proposer_State state;

		public Instance(int ballot, Set<Integer> pSet, Value value, int valueBallot, Set<Integer> acceptSet,
				boolean isSucc, Proposer_State state) {
			super();
			this.ballot = ballot;
			this.pSet = pSet;
			this.value = value;
			this.valueBallot = valueBallot;
			this.acceptSet = acceptSet;
			this.isSucc = isSucc;
			this.state = state;
		}

	}

	private Map<Integer, Instance> instanceState = new HashMap<>();

	// current instance
	private int currentInstance = 0;

	// proposer's id
	private int id;

	// accepter's number
	private int accepterNum;

	// accepters
	private List<InfoObject> accepters;

	// my info
	private InfoObject my;

	// timeout for each phase(ms)
	private int timeout;

	// 准备提交的状态
	private BlockingQueue<Value> readyToSubmitQueue = new ArrayBlockingQueue<>(1);

	// 成功提交的状态
	private BlockingQueue<Value> hasSummitQueue = new ArrayBlockingQueue<>(1);

	// 上一次的提交是否成功
	private boolean isLastSumbitSucc = false;

	// 本节点的accepter
	private Accepter accepter;

	// 组id
	private int groupId;

	// 消息队列，保存packetbean
	private BlockingQueue<PacketBean> msgQueue = new LinkedBlockingQueue<>();

	private BlockingQueue<PacketBean> submitMsgQueue = new LinkedBlockingQueue<>();

	private ObjectSerialize objectSerialize = new ObjectSerializeImpl();

	private Logger logger = Logger.getLogger("MyPaxos");

	// 客户端
	private CommClient client;

	public Proposer(int id, List<InfoObject> accepters, InfoObject my, int timeout, Accepter accepter, int groupId,
			CommClient client) {
		this.id = id;
		this.accepters = accepters;
		this.accepterNum = accepters.size();
		this.my = my;
		this.timeout = timeout;
		this.accepter = accepter;
		this.groupId = groupId;
		this.client = client;
		new Thread(() -> {
			while (true) {
				try {
					PacketBean msg = this.msgQueue.take();
					recvPacket(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		new Thread(() -> {
			while (true) {
				try {
					PacketBean msg = this.submitMsgQueue.take();
					submit((Value) msg.getData());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 向消息队列中插入packetbean
	 * 
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
	 * @throws InterruptedException
	 */
	public void recvPacket(PacketBean bean) throws InterruptedException {
		switch (bean.getType()) {
		case "PrepareResponsePacket":
			PrepareResponsePacket prepareResponsePacket = (PrepareResponsePacket) bean.getData();
			onPrepareResponse(prepareResponsePacket.getId(), prepareResponsePacket.getInstance(),
					prepareResponsePacket.isOk(), prepareResponsePacket.getAb(), prepareResponsePacket.getAv());
			break;
		case "AcceptResponsePacket":
			AcceptResponsePacket acceptResponsePacket = (AcceptResponsePacket) bean.getData();
			onAcceptResponce(acceptResponsePacket.getId(), acceptResponsePacket.getInstance(),
					acceptResponsePacket.isOk());
			break;
		case "SubmitPacket":
			this.submitMsgQueue.add(bean);
			break;
		default:
			System.out.println("unknown type!!!");
			break;
		}
	}

	/**
	 * 客户端向proposer提交想要提交的状态
	 * 
	 * @param object
	 * @return
	 * @throws InterruptedException
	 */
	public Value submit(Value object) throws InterruptedException {
		this.readyToSubmitQueue.put(object);
		beforPrepare();
		Value value = this.hasSummitQueue.take();
		return value;
	}

	/**
	 * 
	 * 在prepare操作之前
	 */
	public void beforPrepare() {
		// 获取accepter最近的一次instance的id
		this.currentInstance = Math.max(this.currentInstance, accepter.getLastInstanceId());
		this.currentInstance++;
		Instance instance = new Instance(1, new HashSet<>(), null, 0, new HashSet<>(), false, Proposer_State.READY);
		this.instanceState.put(this.currentInstance, instance);
		if (this.isLastSumbitSucc == false) {
			// 执行完整的流程
			prepare(this.id, this.currentInstance, 1);
		} else {
			// multi-paxos 中的优化，直接accept
			instance.isSucc = true;
			accept(this.id, this.currentInstance, 1, this.readyToSubmitQueue.peek());
		}
	}

	/**
	 * 将prepare发送给所有的accepter，并设置超时。 如果超时，则判断阶段1是否完成，如果未完成，则ballot加一之后继续执行阶段一。
	 * 
	 * @param instance
	 *            current instance
	 * @param ballot
	 *            prepare's ballot
	 */
	private void prepare(int id, int instance, int ballot) {
		this.instanceState.get(instance).state = Proposer_State.PREPARE;
		try {
			PacketBean bean = new PacketBean("PreparePacket", new PreparePacket(id, instance, ballot));
			byte[] msg = this.objectSerialize.objectToObjectArray(new Packet(bean, groupId, WorkerType.ACCEPTER));
			this.accepters.forEach((info) -> {
				try {
					this.client.sendTo(info.getHost(), info.getPort(), msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		setTimeout(new TimerTask() {

			@Override
			public void run() {
				// retry phase 1 again!
				Instance current = instanceState.get(instance);
				if (current.state == Proposer_State.PREPARE) {
					current.ballot++;
					prepare(id, instance, current.ballot);
				}
			}
		});
	}

	/**
	 * 接收到accepter对于prepare的回复
	 * 
	 * @param id
	 * @param instance
	 * @param ok
	 * @param ab
	 * @param av
	 * @throws InterruptedException
	 */
	public void onPrepareResponse(int peerId, int instance, boolean ok, int ab, Value av) {
		Instance current = this.instanceState.get(instance);
		if (current.state != Proposer_State.PREPARE)
			return;
		if (ok) {
			current.pSet.add(peerId);
			if (ab > current.valueBallot && av != null) {
				current.valueBallot = ab;
				current.value = av;
				current.isSucc = false;
			}
			if (current.pSet.size() >= this.accepterNum / 2 + 1) {
				if (current.value == null) {
					Value object = this.readyToSubmitQueue.peek();
					current.value = object;
					current.isSucc = true;
				}
				accept(id, instance, current.ballot, current.value);
			}
		}
	}

	/**
	 * 向所有的accepter发送accept，并设置状态。
	 * 
	 * @param id
	 * @param instance
	 * @param ballot
	 * @param value
	 */
	private void accept(int id, int instance, int ballot, Value value) {
		this.instanceState.get(instance).state = Proposer_State.ACCEPT;
		try {
			PacketBean bean = new PacketBean("AcceptPacket", new AcceptPacket(id, instance, ballot, value));
			byte[] msg = this.objectSerialize.objectToObjectArray(new Packet(bean, groupId, WorkerType.ACCEPTER));
			this.accepters.forEach((info) -> {
				try {
					this.client.sendTo(info.getHost(), info.getPort(), msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		setTimeout(new TimerTask() {
			@Override
			public void run() {
				// retry phase 2 again!
				Instance current = instanceState.get(instance);
				if (current.state == Proposer_State.ACCEPT) {
					current.ballot++;
					prepare(id, instance, current.ballot);
				}
			}
		});
	}

	/**
	 * 接收到accepter返回的accept响应
	 * 
	 * @param peerId
	 * @param instance
	 * @param ok
	 * @throws InterruptedException
	 */
	public void onAcceptResponce(int peerId, int instance, boolean ok) throws InterruptedException {
		Instance current = this.instanceState.get(instance);
		if (current.state != Proposer_State.ACCEPT)
			return;
		if (ok) {
			current.acceptSet.add(peerId);
			if (current.acceptSet.size() >= this.accepterNum / 2 + 1) {
				// 流程结束
				done(instance);
				if (current.isSucc) {
					this.isLastSumbitSucc = true;
					this.hasSummitQueue.put(this.readyToSubmitQueue.take());
				} else {
					// 说明这个instance的id已经被占有
					this.isLastSumbitSucc = false;
					beforPrepare();
				}
			}
		}
	}

	/**
	 * 本次paxos选举结束
	 */
	public void done(int instance) {
		this.instanceState.get(instance).state = Proposer_State.FINISH;
	}

	/**
	 * set timeout task
	 * 
	 * @param task
	 */
	private void setTimeout(TimerTask task) {
		new Timer().schedule(task, this.timeout);
	}
}
