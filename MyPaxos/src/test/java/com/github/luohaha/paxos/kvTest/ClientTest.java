package com.github.luohaha.paxos.kvTest;

import java.io.IOException;
import java.net.UnknownHostException;

import com.github.luohaha.paxos.exception.PaxosClientNullAddressException;
import com.github.luohaha.paxos.main.MyPaxosClient;
import com.github.luohaha.paxos.utils.client.CommClient;
import com.github.luohaha.paxos.utils.client.CommClientImpl;
import com.google.gson.Gson;

public class ClientTest {

	public static void main(String[] args) {
		try {
			MyPaxosClient client = new MyPaxosClient();
			client.setSendBufferSize(20);
			client.setRemoteAddress("localhost", 33333);
			Gson gson = new Gson();
			client.submit(gson.toJson(new MsgBean("put", "name", "Mike")).getBytes(), 1);
			client.submit(gson.toJson(new MsgBean("put", "name", "Neo")).getBytes(), 1);
			client.submit(gson.toJson(new MsgBean("get", "name", "")).getBytes(), 1);
			client.flush(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PaxosClientNullAddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
