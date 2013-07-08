package org.glowingmonkey.ioiotricks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

public class MNDPPacket extends UDPPacket {
	MACAddress mac;
	String identity;
	String version;
	String platform;
	int uptime;
	String software_id;
	String board;
	byte unpack;
	// IPv6Addr ipv6addr;
	String iface;
	
	MNDPPacket(byte[] framein) {
		super(framein);
		ByteBuffer bb = ByteBuffer.wrap(UDPPayload).order(ByteOrder.BIG_ENDIAN);
		
		short header = bb.getShort();
		short seqno = bb.getShort();
		
		// Process tagged data
		while (bb.hasRemaining()) {
			short tag;
			short len;
			byte[] buf;
			tag = bb.getShort();
			len = bb.getShort();
			buf = new byte[len];
			
			switch (tag) {
				case 1: // MAC-Address
					bb.get(buf);
					mac = new MACAddress(buf);
					break;
				case 5: // Identity
					bb.get(buf);
					identity = new String(buf);
					break;
				case 7: // Version
					bb.get(buf);
					version = new String(buf);
					break;
				case 8: // Platform
					bb.get(buf);
					platform = new String(buf);
					break;
				case 10: // Uptime
					// Uptime is in wrong-endian?!?
					bb.order(ByteOrder.LITTLE_ENDIAN);
					uptime = bb.getInt();
					bb.order(ByteOrder.BIG_ENDIAN);
					break;
				case 11: // Software ID
					bb.get(buf);
					software_id = new String(buf);
					break;
				case 12: // Board
					bb.get(buf);
					board = new String(buf);
					break;
				case 14: // Unpack??
					unpack = bb.get();
					break;
				case 15: // IPv6-Address
					bb.get(buf);
					// can't decode IPv6 yet, punt
					break;
				case 16: // Interface?
					bb.get(buf);
					iface = new String(buf);
			}
		}
	}
	
	
	@Override
	public String toString() {
		return String.format("[MNDP SRC: %s Ident: %s Ver: %s Board: %s IFace: %s Plat: %s Uptime: %d]", mac.toString(), identity, version, board, iface, platform, (long) uptime & 0xFFFFFFFF);
	}

	@Override
	public String toHeaderString() {
		return toString();
	}
	
}
