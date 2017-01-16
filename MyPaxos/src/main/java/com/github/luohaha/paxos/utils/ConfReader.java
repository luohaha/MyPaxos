package com.github.luohaha.paxos.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.github.luohaha.paxos.core.ConfObject;
import com.google.gson.Gson;

public class ConfReader {
	
	/**
	 * 返回配置文件信息
	 * @param filename
	 * @return
	 */
	private static ConfObject read(String filename) {
		return new Gson().fromJson(readFile(filename), ConfObject.class);
	}
	/**
	 * 读取文件中的内容，返回字符串
	 * @param filename
	 * @return
	 */
	public static String readFile(String filename) {
		String ret = "";
		File file = new File(filename);
        BufferedReader reader = null;  
        try {  
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            StringBuffer buffer = new StringBuffer();
            while ((tempString = reader.readLine()) != null) {  
            		buffer.append(tempString);
            }
            ret = buffer.toString();
            reader.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        }
        return ret;
	}
}
