# MyPaxos

My multi-paxos service implement :-)  

## 说明

这是对multi-paxos协议服务的实现，并提供了给了使用者可以拓展的简单接口，以及简单的客户端。本实现有如下特性：

* 实现了multi-paxos中连续同一leader提交时，优化协议流程，将prepare和accept流程，优化到只有accept流程。
* 实现了节点崩溃恢复的机制。
* 提供了简单的易用的拓展接口给使用者，使用者可以基于此实现基于paxos服务的系统。
* 提供了分组功能，单个节点上可以设置多个虚拟分组，各个分组之间逻辑独立。

## paxos协议的简单说明

Paxos算法解决的问题是在一个可能发生上述异常的分布式系统中如何就某个值达成一致，保证不论发生以上任何异常，都不会破坏决议的一致性。一个典型的场景是，在一个分布式数据库系统中，如果各节点的初始状态一致，每个节点都执行相同的操作序列，那么他们最后能得到一个一致的状态。为保证每个节点执行相同的命令序列，需要在每一条指令上执行一个“一致性算法”以保证每个节点看到的指令一致。  

paxos协议中有三种角色：
* proposer : 提案的发起者
* accepter : 提案的接受者
* learner : 提案的学习者

paxos协议保证在每一轮的提案中，只要某一个提案被大于半数的accepter接受，本轮的提案也就生效了，不会再被修改和破坏。具体的算法说明可以看[维基百科](https://zh.wikipedia.org/wiki/Paxos%E7%AE%97%E6%B3%95)。

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

使用MyPaxos协议服务，需要下面几步：
* 实现提交成功时需要执行的回调函数`PaxosCallback`
* 在各个节点上修改配置文件，启动paxos服务器
* 启动客户端，执行提交请求

我在这里使用MyPaxos来实现一个分布式的简单kv存储。

* 配置文件信息

```json
{
    "nodes" : [{
        // 节点1，服务器端口为33333
        "id" : 1,
        "host" : "localhost",
        "port" : 33333
    },
    {
        "id" : 2,
        "host" : "localhost",
        "port" : 33334
    },
    {
        "id" : 3,
        "host" : "localhost",
        "port" : 33335
    }],
    "myid" : 1,    //本节点的id
    "timeout" : 1000, //通信超时
    "learningInterval" : 1000, // learner的学习时间间隔
    "dataDir" : "./dataDir/", // 数据持久化的位置，用于崩溃恢复
    "enableDataPersistence" : false // 是否开启数据持久化功能
}
```

* 提交成功后的回调函数

```java
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
public class ServerTest {
	public static void main(String[] args) {
		MyPaxos server = new MyPaxos("./conf/conf.json");
		// 设置分组group1
		server.setGroupId(1, new KvCallback());
		// 设置分组group2
		server.setGroupId(2, new KvCallback());
		try {
			server.start();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
```

* 启动客户端，并发送请求

```java
public class ClientTest {
	public static void main(String[] args) {
		MyPaxosClient client = new MyPaxosClient("localhost", 33333);
		try {
		    // 发往group1
			client.submit(new Gson().toJson(new MsgBean("put", "name", "Mike")), 1);
			// 发往group2
			client.submit(new Gson().toJson(new MsgBean("put", "name", "Neo")), 2);
			// 发往group1
			client.submit(new Gson().toJson(new MsgBean("get", "name", "")), 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
```

* 结果

节点1

```
paxos server-1 start...
ok
ok
Mike
```

节点2

```
paxos server-2 start...
ok
ok
Mike
```

节点3

```
paxos server-3 start...
ok
ok
Mike
```

## 参考文献和资料

* Lamport, Leslie. "The part-time parliament." ACM Transactions on Computer Systems (TOCS) 16.2 (1998): 133-169.
* Lamport, Leslie. "Paxos made simple." ACM Sigact News 32.4 (2001): 18-25.
* Primi, Marco. Paxos made code. Diss. University of Lugano, 2009.
* Chandra, Tushar D., Robert Griesemer, and Joshua Redstone. "Paxos made live: an engineering perspective." Proceedings of the twenty-sixth annual ACM symposium on Principles of distributed computing. ACM, 2007.
* [微信自研生产级paxos类库PhxPaxos实现原理介绍](http://mp.weixin.qq.com/s?__biz=MzI4NDMyNTU2Mw==&mid=2247483695&idx=1&sn=91ea422913fc62579e020e941d1d059e#rd)
* [Paxos理论介绍(1): 朴素Paxos算法理论推导与证明](https://zhuanlan.zhihu.com/p/21438357?refer=lynncui)
* [Paxos理论介绍(2): Multi-Paxos与Leader](https://zhuanlan.zhihu.com/p/21466932?refer=lynncui)