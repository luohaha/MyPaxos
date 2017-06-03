package com.github.luohaha.paxos.utils.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import com.github.luohaha.client.LightCommClient;
import com.github.luohaha.connection.Conn;
import com.github.luohaha.exception.ConnectionCloseException;
import com.github.luohaha.inter.OnWrite;
import com.github.luohaha.param.ClientParam;

public class ClientImplByLC4J implements CommClient {

	private LightCommClient client;
	private Map<String, Conn> addressToConn = new HashMap<>();

	public ClientImplByLC4J(int ioThreadPoolSize) {
		// TODO Auto-generated constructor stub
		this.client = new LightCommClient(ioThreadPoolSize);
	}

	@Override
	public void sendTo(String ip, int port, byte[] msg) throws ClosedChannelException {
		// TODO Auto-generated method stub
		ClientParam param = new ClientParam();
		param.setLogLevel(Level.WARNING);
		param.setOnConnect(conn -> {
			String key = ip + ":" + port;
			this.addressToConn.put(key, conn);
		});
		String key = ip + ":" + port;
		if (!addressToConn.containsKey(key)) {
			client.connect(ip, port, param);
		}
		int count = 0;
		do {
			try {
				if (count >= 3)
					break;
				while (addressToConn.get(key) == null);
				addressToConn.get(key).write(msg);
				break;
			} catch (ConnectionCloseException e) {
				e.printStackTrace();
				addressToConn.remove(key);
				client.connect(ip, port, param);
				count++;
			}
		} while (true);
	}

}
