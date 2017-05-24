package com.github.luohaha.paxos.utils;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.luohaha.param.ServerParam;
import com.github.luohaha.server.LightCommServer;

public class ServerImplByLC4J implements CommServer {
	
	private BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
	private LightCommServer server;
	private ServerParam serverParam;
	
	public ServerImplByLC4J(int port, int ioThreadPoolSize) throws IOException {
		this.serverParam = new ServerParam("localhost", port);
		this.serverParam.setBacklog(128);
		this.serverParam.setOnRead((conn, data) -> {
			queue.add(data);
		});
		this.serverParam.setOnClose(conn -> {
			try {
				conn.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		this.server = new LightCommServer(serverParam, ioThreadPoolSize);
		this.server.start();
	}

	@Override
	public byte[] recvFrom() throws InterruptedException {
		// TODO Auto-generated method stub
		return this.queue.take();
	}

}
