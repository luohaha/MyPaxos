package com.github.luohaha.paxos.log;

import java.io.IOException;

import com.github.luohaha.paxos.core.Accepter;

public interface MyPaxosLog {
	/**
	 * 获取log文件的位置
	 * @return
	 * 返回文件位置
	 */
	public String getLogFileAddr();
	/**
	 * 从log文件中恢复
	 */
	public void recoverFromLog() throws IOException;
	/**
	 * 每次新增，修改ballot都会调用此函数，将记录加入log
	 * @param instanceId
	 * @param ballot
	 */
	public void setInstanceBallot(int instanceId, int ballot) throws IOException;
	/**
	 * 每次新增，修改acceptedBallot都会调用此函数，将记录加入log
	 * @param instanceId
	 * @param acceptedBallot
	 */
	public void setInstanceAcceptedBallot(int instanceId, int acceptedBallot) throws IOException;
	/**
	 * 每次新增，修改value都会调用此函数，将记录加入log
	 * @param instanceId
	 * @param value
	 */
	public void setInstanceValue(int instanceId, Object value) throws IOException;
}
