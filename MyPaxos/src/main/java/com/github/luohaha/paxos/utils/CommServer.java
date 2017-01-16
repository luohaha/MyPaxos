package com.github.luohaha.paxos.utils;

public interface CommServer {
	public byte[] recvFrom() throws InterruptedException;
}
