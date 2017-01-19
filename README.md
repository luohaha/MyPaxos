# MyPaxos

My multi-paxos service implement :-)  

## 说明

这是对multi-paxos协议服务的实现，并提供了用户可以拓展的简单接口，以及简单的客户端。

## 总体架构和流程示意图

* paxos服务器和客户端

![](http://7xrlnt.com1.z0.glb.clouddn.com/mypaxos.png)

* paxos协议的提交流程

![](http://7xrlnt.com1.z0.glb.clouddn.com/mypaxos-2.png)

* 多个instance的确认

![](http://7xrlnt.com1.z0.glb.clouddn.com/mypaxos-3.png)

* learner的学习

![](http://7xrlnt.com1.z0.glb.clouddn.com/mypaxos-4.png)

## 使用

我在这里使用MyPaxos来实现一个分布式的简单kv存储。

* 配置文件信息

```json
{
    "nodes" : [{
        // 节点1，服务器端口为33333，accepter的端口为33334，proposer的端口为33335
        // learner的端口为33336
        "id" : 1,
        "host" : "localhost",
        "port" : 33333
    },
    {
        "id" : 2,
        "host" : "localhost",
        "port" : 33343
    },
    {
        "id" : 3,
        "host" : "localhost",
        "port" : 33353
    }],
    "myid" : 1,    //本节点的id
    "timeout" : 1000, //通信超时
    "learningInterval" : 1000, // learner的学习时间间隔
    "dataDir" : "./dataDir/", // 数据持久化的位置，用于崩溃恢复
    "enableDataPersistence" : false // 是否开启数据持久化功能
}
```

* 实现状态成功执行接口

```java
public class KvExecutor implements PaxosExecutor {
	
	/**
	 * 使用map来保存key与value映射
	 */
	private Map<String, String> kv = new HashMap<>();
	private Gson gson = new Gson();

	@Override
	public void execute(String msg) {
		/**
		 * 一共提供了三种动作：
		 * get : 获取
		 * put : 添加
		 * delete : 删除
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
```

* 消息格式

```java
public class MsgBean {
	
	private String type;
	private String key;
	private String value;
	public MsgBean(String type, String key, String value) {
		super();
		this.type = type;
		this.key = key;
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
```

* 启动服务器

```java
public static void main(String[] args) {
		MyPaxos server = new MyPaxos(new KvExecutor());
		try {
			server.start();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
```

* 启动客户端，并发送请求

```java
public class ClientTest {
	public static void main(String[] args) {
		MyPaxosClient client = new MyPaxosClient("localhost", 33333);
		try {
			client.submit(new Gson().toJson(new MsgBean("put", "name", "Mike")));
			client.submit(new Gson().toJson(new MsgBean("put", "age", "22")));
			client.submit(new Gson().toJson(new MsgBean("get", "name", "")));
			client.submit(new Gson().toJson(new MsgBean("delete", "name", "")));
			client.submit(new Gson().toJson(new MsgBean("get", "name", "")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
```

* 结果

```
learner-3 start...
proposer-3 start...
accepter-3 start...
paxos server-3 start...
ok
ok
Mike
ok
null
```

## 参考文献和资料

* Lamport, Leslie. "The part-time parliament." ACM Transactions on Computer Systems (TOCS) 16.2 (1998): 133-169.
* Lamport, Leslie. "Paxos made simple." ACM Sigact News 32.4 (2001): 18-25.
* Primi, Marco. Paxos made code. Diss. University of Lugano, 2009.
* Chandra, Tushar D., Robert Griesemer, and Joshua Redstone. "Paxos made live: an engineering perspective." Proceedings of the twenty-sixth annual ACM symposium on Principles of distributed computing. ACM, 2007.
* [微信自研生产级paxos类库PhxPaxos实现原理介绍](http://mp.weixin.qq.com/s?__biz=MzI4NDMyNTU2Mw==&mid=2247483695&idx=1&sn=91ea422913fc62579e020e941d1d059e#rd)
* [Paxos理论介绍(1): 朴素Paxos算法理论推导与证明](https://zhuanlan.zhihu.com/p/21438357?refer=lynncui)
* [Paxos理论介绍(2): Multi-Paxos与Leader](https://zhuanlan.zhihu.com/p/21466932?refer=lynncui)