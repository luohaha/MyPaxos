package com.github.luohaha.paxos.utils.server;

public interface CommServer {
	public byte[] recvFrom() throws InterruptedException;
}
