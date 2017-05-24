package com.github.luohaha.paxos.kvTest;

import java.io.IOException;
import java.net.UnknownHostException;

import com.github.luohaha.paxos.main.MyPaxosClient;
import com.github.luohaha.paxos.utils.CommClient;
import com.github.luohaha.paxos.utils.CommClientImpl;
import com.google.gson.Gson;

public class ClientTest {

	public static void main(String[] args) {
		try {
			MyPaxosClient client = new MyPaxosClient("localhost", 33333);
			client.submit(new Gson().toJson(new MsgBean("put", "name", "Mike")), 1);
			client.submit(new Gson().toJson(new MsgBean("put", "name", "Neo")), 2);
			//client.submit(new Gson().toJson(new MsgBean("delete", "name", "")), 2);
			client.submit(new Gson().toJson(new MsgBean("get", "name", "")), 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
