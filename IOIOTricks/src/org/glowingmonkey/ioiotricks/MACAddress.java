package org.glowingmonkey.ioiotricks;

public class MACAddress {
	byte[] addr;
	
	public MACAddress(byte[] inaddr) {
		addr = inaddr;
	}

	@Override
	public String toString() {
		return String.format("%02x:%02x:%02x:%02x:%02x:%02x", addr[0], addr[1], addr[2], addr[3], addr[4], addr[5]);
	}
	
	
}
