package Com.github.luohaha.paxos.coreTest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import com.github.luohaha.paxos.core.Accepter;
import com.github.luohaha.paxos.core.ConfObject;
import com.github.luohaha.paxos.core.InfoObject;
import com.github.luohaha.paxos.core.Proposer;
import com.github.luohaha.paxos.main.MyPaxosClient;
import com.github.luohaha.paxos.utils.CommClient;
import com.github.luohaha.paxos.utils.CommClientImpl;
import com.github.luohaha.paxos.utils.ConfReader;
import com.google.gson.Gson;

public class ClientTest {
	public static void main(String[] args) throws UnknownHostException, IOException {
		MyPaxosClient client = new MyPaxosClient("localhost", 33333);
		client.submit("hello paxos4");
	}
}
