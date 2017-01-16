package Com.github.luohaha.paxos.coreTest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import com.github.luohaha.paxos.core.Accepter;
import com.github.luohaha.paxos.core.ConfObject;
import com.github.luohaha.paxos.core.InfoObject;
import com.github.luohaha.paxos.core.Proposer;
import com.github.luohaha.paxos.utils.CommClient;
import com.github.luohaha.paxos.utils.CommClientImpl;
import com.github.luohaha.paxos.utils.ConfReader;
import com.google.gson.Gson;

public class CoreTest {
	public static void main(String[] args) throws UnknownHostException, IOException {
		CommClient client = new CommClientImpl();
		client.sendTo("localhost", 33333, "hello paxos!".getBytes());
	}
}
