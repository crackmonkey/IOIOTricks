package org.glowingmonkey.ioiotricks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UDPPacket extends IPv4Packet {
	short srcport;
	byte[] UDPPayload;
	
	UDPPacket(byte[] framein) {
		ByteBuffer bb = ByteBuffer.wrap(framein).order(ByteOrder.BIG_ENDIAN);
		srcip = new IPv4Address(bb.getInt());
		srcport = bb.getShort();
		short payloadlen = bb.getShort();
		UDPPayload = new byte[payloadlen];
		bb.get(UDPPayload);
		
		proto = 17;
	}

	@Override
	public String toString() {
		return String.format("[UDP SRC: %s SrcPort: %d Data: %s]", srcip.toString(), srcport, Util.toHex(UDPPayload));
	}

	public String toHeaderString() {
		return String.format("[UDP SRC: %s SrcPort: %d Len: %d]", srcip.toString(), srcport, UDPPayload.length);
	}
}
