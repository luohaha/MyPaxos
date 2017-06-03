package com.github.luohaha.paxos.utils.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class NonBlockClientImpl implements CommClient {

	private Selector selector;
	private ConcurrentMap<SocketChannel, ByteBuffer> channelMap;
	private BlockingQueue<SocketChannel> channalQueue;

	public NonBlockClientImpl() throws IOException {
		super();
		this.selector = Selector.open();
		this.channelMap = new ConcurrentHashMap<>();
		this.channalQueue = new LinkedBlockingQueue<>();
		new Thread(() -> {
			while (true) {
				try {
					this.selector.select();
					SocketChannel newChan = this.channalQueue.poll();
					if (newChan != null) {
						// 注册新的事件
						newChan.register(this.selector, SelectionKey.OP_CONNECT);
					}
					Set<SelectionKey> keys = this.selector.selectedKeys();
					Iterator<SelectionKey> iterator = keys.iterator();
					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();
						SocketChannel channel = (SocketChannel) key.channel();
						if (key.isConnectable()) {
							try {
								if (channel.finishConnect()) {
									channel.register(this.selector, SelectionKey.OP_WRITE);
								}
							} catch (IOException e) {
								
							}
						} else if (key.isWritable()) {
							// 数据可写
							writeData(channel);
						}
						iterator.remove();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 向channel中非阻塞地写入数据
	 * 
	 * @param channel
	 * @throws IOException
	 */
	private void writeData(SocketChannel channel) throws IOException {
		ByteBuffer buffer = this.channelMap.get(channel);
		if (buffer == null) {
			return;
		}
		if (buffer.hasRemaining()) {
			channel.write(buffer);
		}
		if (!buffer.hasRemaining()) {
			this.channelMap.remove(channel);
			channel.close();
		}
	}

	@Override
	public void sendTo(String ip, int port, byte[] msg) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		ByteBuffer buffer = ByteBuffer.allocate(msg.length);
		buffer.put(msg);
		buffer.flip();
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		this.channelMap.put(socketChannel, buffer);
		this.channalQueue.add(socketChannel);
		// 建立连接
		SocketAddress address = new InetSocketAddress(ip, port);
		socketChannel.connect(address);
		this.selector.wakeup();
	}

	public static void main(String[] args) {
		try {
			CommClient client = new NonBlockClientImpl();
			client.sendTo("localhost", 8888, new String("hello ").getBytes());
			client.sendTo("localhost", 8888, new String("world!").getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
