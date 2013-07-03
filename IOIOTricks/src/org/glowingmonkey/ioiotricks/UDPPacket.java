package org.glowingmonkey.ioiotricks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.util.Log;

public class UDPPacket extends IPv4Packet {
	short srcport;
	byte[] UDPPayload;
	
	UDPPacket(byte[] framein) {
		ByteBuffer bb = ByteBuffer.wrap(framein).order(ByteOrder.BIG_ENDIAN);
		srcip = new IPv4Address(bb.getInt());
		srcport = bb.getShort();
		
		short payloadlen = bb.getShort();
		
		if (payloadlen != bb.remaining()) {
			Log.d("UDPPacket decode", "WTF, W5100 length doesn't match buffer size: " + payloadlen + " vs " + bb.remaining());
		}
		// payload len is wrong? Just copy whatever is left of the buffer
		UDPPayload = new byte[bb.remaining()];
		bb.get(UDPPayload);
		
		proto = 17;
	}
	
	UDPPacket() {
		UDPPayload = new byte[0];
	}

	@Override
	public String toString() {
		return String.format("[UDP SRC: %s SrcPort: %d Data: %s]", srcip.toString(), srcport, Util.toHex(UDPPayload));
	}

	public String toHeaderString() {
		return String.format("[UDP SRC: %s SrcPort: %d Len: %d]", srcip.toString(), srcport, UDPPayload.length);
	}

	public byte[] toBytes() {
		return UDPPayload;
	}
}
