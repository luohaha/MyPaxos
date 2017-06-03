package com.github.luohaha.paxos.utils.serializable;

import java.io.IOException;
import java.io.Serializable;

public class SerializeTest {
	public static void main(String[] args) {
		ObjectSerialize objectSerialize = new ObjectSerializeImpl();
		Name name = new Name("Li", "Hao", new Sex(true));
		try {
			byte[] data = objectSerialize.objectToObjectArray(name);
			Name ret = objectSerialize.byteArrayToObject(data, Name.class);
			Sex sex = (Sex) ret.sex;
			System.out.println(ret.first + " " + ret.second + " " + sex.isMan);
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static class Name implements Serializable {
		String first;
		String second;
		Object sex;
		public Name(String first, String second, Object sex) {
			super();
			this.first = first;
			this.second = second;
			this.sex = sex;
		}
		
	}
	
	static class Sex implements Serializable{
		boolean isMan;

		public Sex(boolean isMan) {
			super();
			this.isMan = isMan;
		}
		
	}
}
