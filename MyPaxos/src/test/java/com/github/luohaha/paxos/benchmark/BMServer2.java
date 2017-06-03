package com.github.luohaha.paxos.benchmark;

import java.io.IOException;
import com.github.luohaha.paxos.main.MyPaxos;

public class BMServer2 {
	public static void main(String[] args) {
		try {
			MyPaxos server = new MyPaxos("./conf/conf2.json");
			server.setGroupId(1, new BMCallback());
			server.start();
		} catch (IOException | InterruptedException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
