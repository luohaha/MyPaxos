package com.github.luohaha.paxos.kvTest;

import java.util.HashMap;
import java.util.Map;

import com.github.luohaha.paxos.core.PaxosCallback;
import com.github.luohaha.paxos.utils.CommClient;
import com.github.luohaha.paxos.utils.CommClientImpl;
import com.github.luohaha.paxos.utils.CommServer;
import com.github.luohaha.paxos.utils.CommServerImpl;
import com.google.gson.Gson;

public class KvCallback implements PaxosCallback {
	/**
	 * 使用map来保存key与value映射
	 */
	private Map<String, String> kv = new HashMap<>();
	private Gson gson = new Gson();

	@Override
	public void callback(String msg) {
		/**
		 * 一共提供了三种动作： get : 获取 put : 添加 delete : 删除
		 */
		MsgBean bean = gson.fromJson(msg, MsgBean.class);
		switch (bean.getType()) {
		case "get":
			System.out.println(kv.get(bean.getKey()));
			break;
		case "put":
			kv.put(bean.getKey(), bean.getValue());
			System.out.println("ok");
			break;
		case "delete":
			kv.remove(bean.getKey());
			System.out.println("ok");
			break;
		default:
			break;
		}
	}

}
