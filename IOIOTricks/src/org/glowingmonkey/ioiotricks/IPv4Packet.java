package org.glowingmonkey.ioiotricks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// IPRaw sockets
public class IPv4Packet {
	IPv4Address srcip;
	IPv4Address dstip;
	short proto;
	byte[] IPPayload;
	
	IPv4Packet(byte[] framein) {
		ByteBuffer bb = ByteBuffer.wrap(framein).order(ByteOrder.BIG_ENDIAN);
		srcip = new IPv4Address(bb.getInt());
		short payloadlen = bb.getShort();
		IPPayload = new byte[payloadlen];
	}
	IPv4Packet() {
		srcip = new IPv4Address(0);
		dstip = new IPv4Address(0);
	}

	@Override
	public String toString() {
		return String.format("SRC: %s DST: unknown Protocol: %02x Data: %s", srcip.toString(), proto, Util.toHex(IPPayload));
	}

}
