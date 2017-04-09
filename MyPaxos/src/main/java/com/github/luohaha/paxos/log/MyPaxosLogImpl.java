package com.github.luohaha.paxos.log;

import com.github.luohaha.paxos.core.Accepter;

public class MyPaxosLogImpl implements MyPaxosLog {

	@Override
	public String getLogFileAddr(Accepter accepter) {
		// TODO Auto-generated method stub
		return accepter.getConfObject().getDataDir() + "accepter-" + 
				accepter.getGroupId() + "-" + accepter.getId() + ".mplog";
	}

	@Override
	public void recoverFromLog(Accepter accepter, String logFileAddr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInstanceBallot(int instanceId, int ballot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInstanceAcceptedBallot(int instanceId, int acceptedBallot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInstanceValue(int instanceId, Object value) {
		// TODO Auto-generated method stub
		
	}

}
