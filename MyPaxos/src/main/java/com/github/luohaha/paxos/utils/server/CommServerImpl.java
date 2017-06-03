package com.github.luohaha.paxos.utils.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class CommServerImpl implements CommServer {

	private ServerSocket server;
	private int port;
	// received data
	private BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
	// thread pool
	private ExecutorService pool = Executors.newCachedThreadPool();

	public CommServerImpl(int port) throws IOException {
		super();
		this.port = port;
		server = new ServerSocket(this.port, 128);
		new Thread(() -> {
			while (true) {
				try {
					Socket client = server.accept();
					pool.execute(new ReadThread(client, queue));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public byte[] recvFrom() throws InterruptedException {
		// TODO Auto-generated method stub
		byte[] msg = this.queue.take();
		return msg;
	}

	class ReadThread implements Runnable {

		private Socket client;
		private BlockingQueue<byte[]> queue;

		public ReadThread(Socket client, BlockingQueue<byte[]> queue) {
			super();
			this.client = client;
			this.queue = queue;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				InputStream inputStream = this.client.getInputStream();
				byte[] buf = new byte[4096];
				int n;
				while ((n = inputStream.read(buf)) >= 0) {
					stream.write(buf, 0, n);
				}
				this.queue.put(stream.toByteArray());
				inputStream.close();
				this.client.close();
			} catch (IOException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

	}

}
