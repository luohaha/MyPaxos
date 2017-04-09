package com.github.luohaha.paxos.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * mmap基础功能实现
 * @author luoyixin
 *
 */
public class MmapTool {
	private RandomAccessFile raf;
	private FileChannel channel;
	private MappedByteBuffer buffer;
	private MappedByteBuffer readBuffer;
	// 每一次请求分配的空间的大小
	private final int CHUNK_SIZE = 1024;
	// 起始位置
	private int start = 0;
	// 最终位置
	private int end = 0;
	
	public MmapTool(String addr) throws IOException {
		this.raf = new RandomAccessFile(addr, "rw");
		this.channel = this.raf.getChannel();
		this.end += CHUNK_SIZE;
		this.buffer = channel.map(MapMode.READ_WRITE, this.start, this.end - this.start);
	}
	
	/**
	 * 将数据写入文件
	 * @param data
	 * @throws IOException
	 */
	public void write(byte[] data) throws IOException {
		while (data.length >= this.end - this.start) {
			extendMemory();
		}
		this.buffer.put(data);
		this.start += data.length;
	}
	
	/**
	 * 读出数据
	 * @return
	 * @throws IOException
	 */
	public byte[] read() throws IOException {
		this.readBuffer = channel.map(MapMode.READ_ONLY, 0, this.start);
		byte[] data = new byte[this.start];
		this.readBuffer.get(data);
		return data;
	}
	
	/**
	 * 拓展存储
	 * @throws IOException
	 */
	public void extendMemory() throws IOException {
		this.end += CHUNK_SIZE;
		this.buffer = channel.map(MapMode.READ_WRITE, start, end - start);
	}

}
