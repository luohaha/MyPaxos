package com.github.luohaha.paxos.core;

public interface PaxosExecutor {
	/**
	 * 执行器，用于执行确定的状态
	 * @param msg
	 */
	public void execute(String msg);
}
