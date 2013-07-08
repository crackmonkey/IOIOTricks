package org.glowingmonkey.ioiotricks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class DHCPPacket extends UDPPacket {
	byte op; // 1 = Request, 2 = Reply
	byte htype; // Hardware type. Only ethernet (1)
	byte hlen; // Hardware address length (6 for ethernet)
	byte hops; // optionally set by relay agents
	int xid; // Transaction ID
	short secs; // Filled in by client, seconds elapsed since client began address acquisition or renewal process.
	short flags;
	IPv4Address ciaddr;
	IPv4Address yiaddr;
	IPv4Address siaddr;
	IPv4Address giaddr;
	byte[] chaddr; // 16 byte client identifier
	final int cookie = 0x63825363;
	byte[] sname; // 64 bytes
	byte[] file; // 128 bytes
	
	
	DHCPPacket(byte[] framein) {
		super(framein);
		ByteBuffer bb = ByteBuffer.wrap(UDPPayload).order(ByteOrder.BIG_ENDIAN);
		
		op = bb.get();
		htype = bb.get();
		hlen = bb.get();
		hops = bb.get();
		xid = bb.getInt();
		secs = bb.getShort();
		flags = bb.getShort();
		ciaddr = new IPv4Address(bb.getInt());
		yiaddr = new IPv4Address(bb.getInt());
		siaddr = new IPv4Address(bb.getInt());
		giaddr = new IPv4Address(bb.getInt());
		chaddr = new byte[16];
		bb.get(chaddr);
		sname = new byte[64];
		bb.get(sname);
		file = new byte[128];
		bb.get(file);
		// TODO: parse options here, including going back into sname and file if they have been comendeered. 
		
	}

	public DHCPPacket() {
		super();
		UDPPayload = new byte[244]; // just big enough for the mandatory bits
		op = 1;
		htype = 1;
		hlen = 6;
		hops = 0;
		
		Random gen = new Random();
		xid = gen.nextInt();
		
		secs = 0;
		flags = 0;
		
		ciaddr = new IPv4Address(0);
		yiaddr = new IPv4Address(0);
		siaddr = new IPv4Address(0);
		giaddr = new IPv4Address(0);
		
		chaddr = new byte[16];
		// TODO: Random client id for now.
		gen.nextBytes(chaddr);
		
		sname = new byte[64];
		file = new byte[128];
	}


	public byte[] toBytes() {
		ByteBuffer bb = ByteBuffer.wrap(UDPPayload).order(ByteOrder.BIG_ENDIAN);
		
		bb.put(op);
		bb.put(htype);
		bb.put(hlen);
		bb.put(hops);
		bb.putInt(xid);
		bb.putShort(secs);
		bb.putShort(flags);
		bb.put(ciaddr.addr);
		bb.put(yiaddr.addr);
		bb.put(siaddr.addr);
		bb.put(giaddr.addr);
		bb.put(chaddr);
		bb.put(sname);
		bb.put(file);
		bb.putInt(cookie);
		
		// DHCP Message Type
		bb.put((byte)53);
		bb.put((byte) 1); // Length 1
		bb.put((byte) 1); // DISCOVER
		
		// DHCP end
		bb.put((byte)255);
		
		// build options and append them
		
		return UDPPayload;
	}
	
	@Override
	public String toString() {
		return String.format("[DHCP SRC: %s SrcPort: %d Len: %d ciaddr: %s yiaddr: %s siaddr: %s giaddr: %s sname: %s file: %s options: %s]", srcip.toString(), srcport, UDPPayload.length, ciaddr.toString(), yiaddr.toString(), siaddr.toString(), giaddr.toString(), sname.toString(), file.toString(), Util.toHex(UDPPayload));
	}

	public String toHeaderString() {
		return String.format("[DHCP SRC: %s SrcPort: %d Len: %d ciaddr: %s yiaddr: %s siaddr: %s giaddr: %s]", srcip.toString(), srcport, UDPPayload.length, ciaddr.toString(), yiaddr.toString(), siaddr.toString(), giaddr.toString());
	}
}
