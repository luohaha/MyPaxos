package com.github.luohaha.paxos.utils.serializable;

import java.io.IOException;

public interface ObjectSerialize {
	public byte[] objectToObjectArray(Object object) throws IOException;
	public <T> T byteArrayToObject(byte[] byteArray, Class<T> type) throws ClassNotFoundException, IOException;
}
