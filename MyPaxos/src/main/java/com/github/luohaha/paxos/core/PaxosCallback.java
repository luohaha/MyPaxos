package com.github.luohaha.paxos.core;

public interface PaxosCallback {
	/**
	 * 执行器，用于执行确定的状态
	 * @param msg
	 */
	public void callback(byte[] msg);
}
