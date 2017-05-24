package com.github.luohaha.paxos.utils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;

import com.github.luohaha.client.LightCommClient;
import com.github.luohaha.connection.Conn;
import com.github.luohaha.exception.ConnectionCloseException;
import com.github.luohaha.inter.OnWrite;
import com.github.luohaha.param.ClientParam;

public class ClientImplByLC4J implements CommClient {
	
	private LightCommClient client;
	
	public ClientImplByLC4J(int ioThreadPoolSize) throws IOException {
		// TODO Auto-generated constructor stub
		this.client = new LightCommClient(ioThreadPoolSize);
	}

	@Override
	public void sendTo(String ip, int port, byte[] msg) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		ClientParam param = new ClientParam();
		param.setOnWrite(conn -> {
			try {
				conn.write(msg);
				conn.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		this.client.connect(ip, port, param);
	}

}
