package Com.github.luohaha.paxos.coreTest;

import java.io.IOException;

import com.github.luohaha.paxos.main.MyPaxos;

public class ServerTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		MyPaxos myPaxos = new MyPaxos(new TestExecutor());
		myPaxos.start();
	}
}
