package com.github.luohaha.paxos.utils.client;

import java.io.IOException;
import java.net.UnknownHostException;

public interface CommClient {
	public void sendTo(String ip, int port, byte[] msg) throws UnknownHostException, IOException;
}
