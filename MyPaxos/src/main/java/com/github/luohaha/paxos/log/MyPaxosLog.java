package com.github.luohaha.paxos.log;

import com.github.luohaha.paxos.core.Accepter;

public interface MyPaxosLog {
	/**
	 * 获取log文件的位置
	 * @param accepter
	 * 对应的accepter
	 * @return
	 * 返回文件位置
	 */
	public String getLogFileAddr(Accepter accepter);
	/**
	 * 从log文件中恢复
	 * @param logFileAddr
	 */
	public void recoverFromLog(Accepter accepter, String logFileAddr);
	/**
	 * 每次新增，修改ballot都会调用此函数，将记录加入log
	 * @param instanceId
	 * @param ballot
	 */
	public void setInstanceBallot(int instanceId, int ballot);
	/**
	 * 每次新增，修改acceptedBallot都会调用此函数，将记录加入log
	 * @param instanceId
	 * @param acceptedBallot
	 */
	public void setInstanceAcceptedBallot(int instanceId, int acceptedBallot);
	/**
	 * 每次新增，修改value都会调用此函数，将记录加入log
	 * @param instanceId
	 * @param value
	 */
	public void setInstanceValue(int instanceId, Object value);
}
