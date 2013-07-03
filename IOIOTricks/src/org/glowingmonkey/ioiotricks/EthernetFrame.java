package org.glowingmonkey.ioiotricks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EthernetFrame {
	byte[] frame;
	MACAddress src;
	MACAddress dst;
	short ethertype;
	byte[] ethernetPayload;
	
	public EthernetFrame(byte[] framein) {
		ByteBuffer bb = ByteBuffer.wrap(framein).order(ByteOrder.BIG_ENDIAN);
		byte[] mac;
		frame = framein;
		
		// lying about the header?
		//int payloadlen = bb.getShort() & 0xffff;
		//Log.d("EthernetFrame", "Payload length: " + Util.toHex(payloadlen));
		
		mac = new byte[6]; // DST MAC
		bb.get(mac);
		dst = new MACAddress(mac);
		mac = new byte[6]; // SRC MAC
		bb.get(mac);
		src = new MACAddress(mac);
		ethertype = bb.getShort();
		
		ethernetPayload = new byte[framein.length-16]; // 16 = 2 MAC addresses x6 bytes + 2 bytes header + 2 byte ethertype
		bb.get(ethernetPayload);
	}
	
	@Override
	public String toString() {
		return String.format("[MAC SRC: %s DST: %s Ethertype: %s Data: %s]", src.toString(), dst.toString(), Util.toHex(ethertype), Util.toHex(ethernetPayload));
	}
	public String toHeaderString() {
		return String.format("[MAC SRC: %s DST: %s Ethertype: %s Datalen: %d]", src.toString(), dst.toString(), Util.toHex(ethertype), ethernetPayload.length);
	}
}
