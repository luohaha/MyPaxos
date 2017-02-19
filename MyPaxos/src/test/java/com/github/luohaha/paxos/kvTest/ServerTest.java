package com.github.luohaha.paxos.kvTest;

import java.io.IOException;

import com.github.luohaha.paxos.main.MyPaxos;

public class ServerTest {
	public static void main(String[] args) {
		MyPaxos server = new MyPaxos("./conf/conf.json");
		server.setGroupId(1, new KvCallback());
		server.setGroupId(2, new KvCallback());
		try {
			server.start();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
