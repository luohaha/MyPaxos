package com.github.luohaha.paxos.kvTest;

import java.io.IOException;

import com.github.luohaha.paxos.main.MyPaxosClient;
import com.google.gson.Gson;

public class ClientTest {
	public static void main(String[] args) {
		MyPaxosClient client = new MyPaxosClient("localhost", 33333);
		try {
			client.submit(new Gson().toJson(new MsgBean("put", "name", "Mike")));
			client.submit(new Gson().toJson(new MsgBean("put", "age", "22")));
			client.submit(new Gson().toJson(new MsgBean("get", "name", "")));
			client.submit(new Gson().toJson(new MsgBean("delete", "name", "")));
			client.submit(new Gson().toJson(new MsgBean("get", "name", "")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
