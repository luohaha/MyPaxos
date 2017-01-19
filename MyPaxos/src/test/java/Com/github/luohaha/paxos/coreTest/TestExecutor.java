package Com.github.luohaha.paxos.coreTest;

import com.github.luohaha.paxos.core.PaxosExecutor;

public class TestExecutor implements PaxosExecutor {

	@Override
	public void execute(String msg) {
		// TODO Auto-generated method stub
		System.out.println("收到 " + msg);
	}

}
