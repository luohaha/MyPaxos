package com.github.luohaha.paxos.kvTest;

import java.io.IOException;

import com.github.luohaha.paxos.main.MyPaxos;

public class ServerTest {
	public static void main(String[] args) {
		MyPaxos server = new MyPaxos(new KvCallback(), "./conf/conf.json");
		try {
			server.start();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
