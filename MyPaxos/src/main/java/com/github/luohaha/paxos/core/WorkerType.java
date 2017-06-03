package com.github.luohaha.paxos.core;

import java.io.Serializable;

public enum WorkerType implements Serializable {
	PROPOSER, ACCEPTER, LEARNER, SERVER
}
