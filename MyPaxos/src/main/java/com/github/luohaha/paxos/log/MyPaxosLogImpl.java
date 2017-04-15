package com.github.luohaha.paxos.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.github.luohaha.paxos.core.Accepter;
import com.github.luohaha.paxos.utils.MmapTool;

public class MyPaxosLogImpl implements MyPaxosLog {
	
	/**
	 * 每个log的结构
	 * @author luoyixin
	 *
	 */
	static class Log {
		// 类型字段 0 -> ballot, 1 -> AcceptBallot, 2 -> value
		int type;
		// value的长度
		int len;
		// instance的id
		int instanceId;
		// 值
		Object value;
		public Log(int type, int len, int instanceId, Object value) {
			super();
			this.type = type;
			this.len = len;
			this.instanceId = instanceId;
			this.value = value;
		}
	}
	// 魔数，在每个log的开头
	private final static byte MAGIC_NUMBER = (byte) 0xd4;
	
	private MmapTool tool;
	private Accepter accepter;
	
	public MyPaxosLogImpl(Accepter accepter) throws IOException {
		// TODO Auto-generated constructor stub
		this.accepter = accepter;
		this.tool = new MmapTool(getLogFileAddr());
	}
	
	public MyPaxosLogImpl(String addr) throws IOException {
		// TODO Auto-generated constructor stub
		this.tool = new MmapTool(addr);
	}

	@Override
	public String getLogFileAddr() {
		// TODO Auto-generated method stub
		return this.accepter.getConfObject().getDataDir() + "accepter-" + 
				this.accepter.getGroupId() + "-" + this.accepter.getId() + ".mplog";
	}

	@Override
	public void recoverFromLog() throws IOException {
		// TODO Auto-generated method stub
		byte[] data = this.tool.read();
		int pos = 0;
		while (data[pos] == MAGIC_NUMBER) {
			Log log = readEachLog(data, pos + 1);
			pos += log.len + 8 + 1;
			System.out.println(log.type + " " +log.instanceId + "->" + log.value);
		}
	}
	
	private Log readEachLog(byte[] data, int start) throws IOException {
		byte type = (byte) (data[start] & 0x03);
		if (type == 0x00) {
			return new Log(0, byteToIntegerFromPos(data, start) >> 2, byteToIntegerFromPos(data, start + 4),
					byteToIntegerFromPos(data, start + 8));
		} else if (type == 0x01) {
			return new Log(1, byteToIntegerFromPos(data, start) >> 2, byteToIntegerFromPos(data, start + 4),
					byteToIntegerFromPos(data, start + 8));
		} else {
			int len = byteToIntegerFromPos(data, start) >> 2;
			Object value = byteToObject(data, start + 8, len);
			return new Log(2, len, byteToIntegerFromPos(data, start + 4), value);
		}
	}

	@Override
	public void setInstanceBallot(int instanceId, int ballot) throws IOException {
		// TODO Auto-generated method stub
		byte[] data = new byte[3 * 4];
		data[0] = (4 << 2) | 0;
		copyIntegerToByte(data, 4, instanceId);
		copyIntegerToByte(data, 8, ballot);
		this.tool.write(new byte[]{MAGIC_NUMBER});
		this.tool.write(data);
	}

	@Override
	public void setInstanceAcceptedBallot(int instanceId, int acceptedBallot) throws IOException {
		// TODO Auto-generated method stub
		byte[] data = new byte[3 * 4];
		data[0] = (4 << 2) | 1;
		copyIntegerToByte(data, 4, instanceId);
		copyIntegerToByte(data, 8, acceptedBallot);
		this.tool.write(new byte[]{MAGIC_NUMBER});
		this.tool.write(data);
	}

	@Override
	public void setInstanceValue(int instanceId, Object value) throws IOException {
		// TODO Auto-generated method stub
		byte[] valueByte = objectToByte(value);
		byte[] data = new byte[2 * 4 + valueByte.length];
		int tmp = (valueByte.length << 2) | 2;
		data[0] = (byte) (tmp & 0xff);
		data[1] = (byte) ((tmp >>> 8) & 0xff);
		data[2] = (byte) ((tmp >>> 16) & 0xff);
		data[3] = (byte) ((tmp >>> 24) & 0xff);
		copyIntegerToByte(data, 4, instanceId);
		for (int i = 0; i < valueByte.length; i++) {
			data[i + 8] = valueByte[i];
		}
		this.tool.write(new byte[]{MAGIC_NUMBER});
		this.tool.write(data);
	}
	
	/**
	 * 将num复制到byte数组的指定位置
	 * @param data
	 * @param pos
	 * @param num
	 * @throws IOException
	 */
	private void copyIntegerToByte(byte[] data, int pos, int num) throws IOException {
		byte[] numByte = integerToByte(num);
		for (int i = 0; i < 4; i++) {
			data[i + pos] = numByte[i];
		}
	}

	/**
	 * object 转 byte
	 * @param object
	 * @return
	 * @throws IOException
	 */
	private byte[] objectToByte(Object object) throws IOException {
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		doutput.writeUTF(object.toString());
		return boutput.toByteArray();
	}
	
	/**
	 * byte转object
	 * @param data
	 * @param start
	 * @param len
	 * @return
	 * @throws IOException 
	 */
	private Object byteToObject(byte[] data, int start, int len) throws IOException {
		ByteArrayInputStream binput = new ByteArrayInputStream(data, start, len);
		DataInputStream dinput = new DataInputStream(binput);
		return dinput.readUTF();
	}
	
	/**
	 * int 转 byte，采用小端字节序
	 * @param num
	 * @return
	 */
	private byte[] integerToByte(int num) {
		return new byte[] {(byte) (num & 0xff),
				           (byte) ((num >> 8) & 0xff),
				           (byte) ((num >> 16) & 0xff),
				           (byte) ((num >> 24) & 0xff)};
	}
	
	/**
	 * byte转int，采用小端字节序
	 * @param data
	 * @return
	 */
	private int byteToInteger(byte[] data) {
		return byteToIntegerFromPos(data, 0);
	}
	
	/**
	 * byte转int，采用小端字节序，从start位置开始
	 * @param data
	 * @param start
	 * @return
	 */
	private int byteToIntegerFromPos(byte[] data, int start) {
		return data[start] & 0xff |
			   (data[start + 1] & 0xff) << 8 |
			   (data[start + 2] & 0xff) << 16 |
			   (data[start + 3] & 0xff) << 24;
	}
	
	public static void main(String[] args) {
		try {
			MyPaxosLog myPaxosLog = new MyPaxosLogImpl("./dataDir/test.log");
			//myPaxosLog.setInstanceAcceptedBallot(1, 1);
			//myPaxosLog.setInstanceBallot(1, 3);		
			myPaxosLog.setInstanceValue(1, "hello");
			myPaxosLog.setInstanceValue(2, "paxos");
			myPaxosLog.setInstanceBallot(3, 1);
			myPaxosLog.setInstanceAcceptedBallot(4, 2);
			myPaxosLog.recoverFromLog();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
