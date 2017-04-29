package com.github.luohaha.paxos.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NonBlockServerImpl implements CommServer {

	private Selector selector;
	private ServerSocketChannel channel;
	// 分配指定大小的缓冲区
	private ByteBuffer buffer = ByteBuffer.allocate(1024);
	// 读取出来的消息
	private BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
	// 读出来的数据
	private Map<SelectionKey, String> map = new HashMap<>();

	public NonBlockServerImpl(int port) throws IOException {
		this.selector = Selector.open();
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		this.channel.socket().bind(new InetSocketAddress(port), 32);
		this.channel.register(this.selector, SelectionKey.OP_ACCEPT);

		new Thread(() -> {
			while (true) {
				try {
					this.selector.select();
					Set<SelectionKey> keys = this.selector.selectedKeys();
					Iterator<SelectionKey> iterator = keys.iterator();
					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();
						readData(key);
						iterator.remove();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void readData(SelectionKey key) throws IOException, InterruptedException {
		if (key.isAcceptable()) {
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			SocketChannel channel = server.accept();
			channel.configureBlocking(false);
			channel.register(this.selector, SelectionKey.OP_READ);
		} else if (key.isReadable()) {
			SocketChannel channel = (SocketChannel) key.channel();
			int count = channel.read(buffer);
			if (count >= 0) {
				buffer.flip();
				if (!map.containsKey(key))
					map.put(key, "");
				byte[] data = new byte[buffer.remaining()];
				buffer.get(data);
				map.put(key, map.get(key) + new String(data));
			} else {
				// 读取结束
				String data = map.remove(key);
				if (data != null) {
					this.queue.put(data.getBytes());
				}
				channel.close();
			}
			buffer.clear();
		}
	}

	@Override
	public byte[] recvFrom() throws InterruptedException {
		// TODO Auto-generated method stub
		byte[] msg = this.queue.take();
		return msg;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		CommServer commServer = new NonBlockServerImpl(8888);
		while (true) {
			System.out.println(new String(commServer.recvFrom()));
		}
	}
}
