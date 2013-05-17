package org.glowingmonkey.ioiotricks;

public class Registers {
	public final static byte BIT0 = 1;
	public final static byte BIT1 = 2;
	public final static byte BIT2 = 4;
	public final static byte BIT3 = 8;
	public final static byte BIT4 = 16;
	public final static byte BIT5 = 32;
	public final static byte BIT6 = 64;
	public final static byte BIT7 = (byte) 128;
	
	public final static byte WRITE_CMD = (byte) 0xF0;
	public final static byte READ_CMD = (byte) 0x0F;
	
	// Mode
	public final static short MR = 0x0000;
	public final static byte MR_RST = BIT7;
	public final static byte MR_PB = BIT4;
	public final static byte MR_PPPoE = BIT3;
	public final static byte MR_AI = BIT1;
	public final static byte MR_IND = BIT0;
	
	// Gateway Address
	public final static short GAR0 = 0x0001;
	public final static short GAR1 = 0x0002;
	public final static short GAR2 = 0x0003;
	public final static short GAR3 = 0x0004;
	
	// Subnet mask Address
	public final static short SUBR0 = 0x0005;
	public final static short SUBR1 = 0x0006;
	public final static short SUBR2 = 0x0007;
	public final static short SUBR3 = 0x0008;
	
	// Source Hardware Address
	public final static short SHAR0 = 0x0009;
	public final static short SHAR1 = 0x000a;
	public final static short SHAR2 = 0x000b;
	public final static short SHAR3 = 0x000c;
	public final static short SHAR4 = 0x000d;
	public final static short SHAR5 = 0x000e;
	
	// Source IP Address
	public final static short SIPR0 = 0x000f;
	public final static short SIPR1 = 0x0010;
	public final static short SIPR2 = 0x0011;
	public final static short SIPR3 = 0x0012;
	
	// Interrupt
	public final static short IR = 0x0015;
	// Interrupt Mask
	public final static short IMR = 0x0016;
	
	// Retry Time
	public final static short RTR0 = 0x0017;
	public final static short RTR1 = 0x0018;
	// Retry Count
	public final static short RCR = 0x0019;
	
	// RX Memory Size
	public final static short RMSR = 0x001a;
	// TX Memory Size
	public final static short TMSR = 0x001b;
	
	// Authentication Type in PPPoE
	public final static short PATR0 = 0x001c;
	public final static short PATR1 = 0x001d;
	
	// PPP LCP Request Timer
	public final static short PTIMER = 0x0028;
	// PPP LCP Magic number
	public final static short PMAGIC = 0x0029;
	
	// Unreachable IP Address
	public final static short UIPR0 = 0x002a;
	public final static short UIPR1 = 0x002b;
	public final static short UIPR2 = 0x002c;
	public final static short UIPR3 = 0x002d;
	
	// Unreachable Port
	public final static short UPORT0 = 0x002e;
	public final static short UPORT1 = 0x002f;
	
	
	// Socket registers
	public final static short SOCKET0 = 0x0400;
	public final static short SOCKET1 = 0x0500;
	public final static short SOCKET2 = 0x0600;
	public final static short SOCKET3 = 0x0700;
	
	// Socket Mode 
	public final static short S_MR = 0x00;
	public final static byte S_MR_MULTI =	BIT7;
	public final static byte S_MR_MF = 		BIT6;
	public final static byte S_MR_NDMC =	BIT5;
	public final static byte S_P_CLOSED =	0;
	public final static byte S_P_TCP =		BIT0;
	public final static byte S_P_UDP =		BIT1;
	public final static byte S_P_IPRAW =	BIT0 | BIT1;
	public final static byte S_P_MACRAW =	BIT2;
	public final static byte S_P_PPPoE =	BIT2 | BIT0;

	// Socket Command
	public final static short S_CR = 		0x01;
	public final static byte S_CR_OPEN = 	0x01;
	public final static byte S_CR_LISTEN = 	0x02;
	public final static byte S_CR_CONNECT = 0x04;
	public final static byte S_CR_DISCON = 	0x08;
	public final static byte S_CR_CLOSE = 	0x10;
	public final static byte S_CR_SEND = 	0x20;
	public final static byte S_CR_SEND_MAC =0x21;
	public final static byte S_CR_SEND_KEEP =0x22;
	public final static byte S_CR_RECV = 	0x40;
	
	// Socket Interrupt
	public final static byte S_IR = 			0x02;
	public final static byte S_IR_SEND_OK = 	BIT4;
	public final static byte S_IR_TIMEOUT = 	BIT3;
	public final static byte S_IR_RECV =	 	BIT2;
	public final static byte S_IR_DISCON = 	BIT1;
	public final static byte S_IR_CON =	 	BIT0;
	
	// Socket Status 
	public final static short S_SR = 			0x03;
	public final static byte S_SR_CLOSED =		0x00;
	public final static byte S_SR_INIT =		0x13;
	public final static byte S_SR_LISTEN =		0x14;
	public final static byte S_SR_ESTABLISHED =	0x17;
	public final static byte S_SR_CLOSED_WAIT =	0x1c;
	public final static byte S_SR_UDP =			0x22;
	public final static byte S_SR_IPRAW =		0x32;
	public final static byte S_SR_MACRAW =		0x42;
	public final static byte S_SR_PPPOE =		0x5f;
	public final static byte S_SR_SYNSENT =		0x15;
	public final static byte S_SR_SYNRECV =		0x16;
	public final static byte S_SR_FIN_WAIT =	0x18;
	public final static byte S_SR_CLOSING =		0x1a;
	public final static byte S_SR_TIME_WAIT =	0x1b;
	public final static byte S_SR_LAST_ACK =	0x1d;
	public final static byte S_SR_ARP =			0x01;
	
	// Socket Source Port
	public final static short S_PORT0 = 0x04;
	public final static short S_PORT1 = 0x05;
	
	// Socket Destination Hardware Address
	public final static short S_DHAR0 = 0x06;
	public final static short S_DHAR1 = 0x07;
	public final static short S_DHAR2 = 0x08;
	public final static short S_DHAR3 = 0x09;
	public final static short S_DHAR4 = 0x0a;
	public final static short S_DHAR5 = 0x0b;
	
	// Socket Destination IP Address
	public final static short S_DIPR0 = 0x0c;
	public final static short S_DIPR1 = 0x0d;
	public final static short S_DIPR2 = 0x0e;
	public final static short S_DIPR3 = 0x0f;
	
	// Socket Destination Port
	public final static short S_DPORT0 = 0x10;
	public final static short S_DPORT1 = 0x11;
	
	// Socket Maximum Segment Size
	public final static short S_MSSR0 = 0x12;
	public final static short S_MSSR1 = 0x13;
	
	// Socket Protocol in IP Raw mode
	public final static short S_PROTO = 0x14;
	
	// Socket IP TOS
	public final static short S_TOS = 0x15;
	
	// Socket IP TTL
	public final static short S_TTL = 0x16;
	
	// Socket TX Free Size
	public final static short S_TX_FSR0 = 0x20;
	public final static short S_TX_FSR1 = 0x21;
	
	// Socket TX Read Pointer
	public final static short S_TX_RD0 = 0x22;
	public final static short S_TX_RD1 = 0x23;
	
	// Socket TX Write Pointer
	public final static short S_RX_WR0 = 0x24;
	public final static short S_RX_WR1 = 0x25;

	// Socket RX Received Size
	public final static short S_RX_RSR0 = 0x26;
	public final static short S_RX_RSR1 = 0x27;
	
	// Socket RX Read Pointer
	public final static short S_RX_RD0 = 0x28;
	public final static short S_RX_RD1 = 0x28;
	
}
