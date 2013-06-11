package org.glowingmonkey.ioiotricks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IPv4Address {
	byte[] addr;

	public IPv4Address(byte[] a) {
		super();
		this.addr = a;
	}

	public IPv4Address(int intaddr) {
		addr = new byte[4];
		ByteBuffer bb = ByteBuffer.wrap(addr).order(ByteOrder.BIG_ENDIAN);
		bb.putInt(intaddr);
	}

	@Override
	public String toString() {
		return addr[0] + "." + addr[1] + "." + addr[2] + "." + addr[3];
	}

}
