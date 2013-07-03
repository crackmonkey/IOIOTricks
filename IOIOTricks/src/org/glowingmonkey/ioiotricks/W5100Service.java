package org.glowingmonkey.ioiotricks;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalInput.Spec.Mode;
import ioio.lib.api.SpiMaster;
import ioio.lib.api.SpiMaster.Rate;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class W5100Service extends IOIOService {
	protected static final String LOG_MESSAGE = "org.glowingmonkey.ioiotricks.action.LOG";
	protected static final String LOG_TEXT = "org.glowingmonkey.ioiotricks.fondler.LOGLINE";
	static final int MSG_RESET = 1; // Reset the controller
	static final int MSG_DHCP = 2; // Send DHCP Discover
	
	public Queue<IPv4Packet> txqueue = new LinkedBlockingQueue<IPv4Packet>(); ;

	Messenger messenger;

    /**
     * Handler of incoming messages from clients.
     */
    static class IncomingHandler extends Handler {
        WeakReference<W5100Service> mFrag;

        IncomingHandler(W5100Service aFragment) {
            mFrag = new WeakReference<W5100Service>(aFragment);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESET:
                    break;
                case MSG_DHCP:
                	W5100Service svc = mFrag.get();
                	if (svc == null) {
                		// nevermind, we lost the service, ignore this
                		return;
                	}
                	sendDhcpDiscover(svc);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

 
    public static void sendDhcpDiscover(W5100Service svc) {
    	Log.d("sendDhcpDiscover", "sending a DHCP discover");
    	DHCPPacket dhcppkt = new DHCPPacket();
    	// Discover
    	dhcppkt.op = 1;
    	dhcppkt.chaddr = new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    	byte[] broadcast = {(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff};
    	dhcppkt.dstip = new IPv4Address(broadcast);
    	dhcppkt.srcport = (short) 67;
    	
    	svc.txqueue.add(dhcppkt);
	}

	/**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));
     /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		messenger = (Messenger) extras.get("MESSENGER");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			private SpiMaster spi;
			
			static final int misoPin = 30;
			static final int mosiPin = 31;
			static final int clkPin = 32;
			int ssPin = 33;

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
				spi = ioio_.openSpiMaster(new DigitalInput.Spec(misoPin,
						Mode.PULL_UP), new DigitalOutput.Spec(mosiPin),
						new DigitalOutput.Spec(clkPin),
						new DigitalOutput.Spec[] { new DigitalOutput.Spec(ssPin) },
						new SpiMaster.Config(Rate.RATE_1M, false, false));
				
				// Reset the chip
				W5100_write(Registers.MR, Registers.MR_RST);
				W5100_write(Registers.IMR, (byte) 0xdf);
				W5100_write(Registers.RTR0, new byte[] {0x07,(byte) 0xd0});
				//W5100_write(Registers.RTR1, (byte) 0xd0);
				W5100_write(Registers.RCR, (byte) 0x08);
				
				// set a MAC
				W5100_write(Registers.SHAR0, new byte[] {(byte) 0xde,(byte) 0xad,(byte) 0xbe,(byte) 0xef,0,1});
				
				// Set buffer sizes, 2k to each socket
				W5100_write(Registers.TMSR,(byte)0x55);
				W5100_write(Registers.RMSR,(byte)0x55);
				
				// set an IP
				W5100_write(Registers.SIPR0, new byte[] {10,20,30,44});
				// netmask
				W5100_write(Registers.SUBR0, new byte[] {(byte)255,(byte)255,(byte)255,0});
				// Gateway
				W5100_write(Registers.GAR0, new byte[] {10,20,30,1});
				
				//setupMACSocket(); // Socket 0, mac raw
				setupUDPSocket(1, (short)67); // Socket 1, DHCP to request
				setupUDPSocket(2, (short)68); // Socket 2, DHCP to response
				setupUDPSocket(3, (short)138); // Socket 2, Browser
				
				appLog("Setup complete");
			};

			@Override
			public void loop() throws ConnectionLostException,
					InterruptedException {
				byte IR = W5100_read8(Registers.IR);
				//appLog("W5100 IR: 0x"+toHex(IR));

				if ((IR & Registers.IR_S0_INT) != 0){
					handleSocketInt(0);
				}
				if ((IR & Registers.IR_S1_INT) != 0){
					handleSocketInt(1);
				}
				if ((IR & Registers.IR_S2_INT) != 0){
					handleSocketInt(2);
				}
				if ((IR & Registers.IR_S3_INT) != 0){
					handleSocketInt(3);
				}
				
				if (!txqueue.isEmpty()) {
					//IPv4Packet pkt = txqueue.poll();
					UDPPacket pkt = (UDPPacket)txqueue.poll();
					// sendUDP(int socket, IPv4Address dst, short port, byte[] payload)
					Log.d("loop()", "Found a packet in the queue, attempting to transmit");
					sendUDP(2, pkt.dstip, pkt.srcport, pkt.toBytes());
					appLog("Transmitted: " + pkt.toHeaderString());
				}
				
				Thread.sleep(100);
			}

			private void handleSocketInt(int socket) throws ConnectionLostException, InterruptedException {
				byte S_IR = W5100_read8(SOCKET_REG(socket,Registers.S_IR));
				appLog("W5100 S"+Integer.toString(socket)+"_IR: 0x"+Util.toHex(S_IR));
				StringBuilder events = new StringBuilder();
				events.append("Interrupt on "+Integer.toString(socket));
				if ((S_IR & Registers.S_IR_CON) != 0) {
					events.append("(CON)");
				}
				if ((S_IR & Registers.S_IR_DISCON) != 0) {
					events.append("(DISCON)");
				}
				if ((S_IR & Registers.S_IR_RECV) != 0) {
					int headerlen = 0;
					short RSR = W5100_read16(SOCKET_REG(socket,Registers.S_RX_RSR0));
					short RD = W5100_read16(SOCKET_REG(socket,Registers.S_RX_RD0));
					short off = (short) (RD & Registers.SOCKET_RX_MASKS[socket]);
					events.append("(RECV "+Util.toHex(RSR)+" at "+Util.toHex(off)+")");
					// actually read the data from address Registers.SOCKET_RX_BASES[socket]+off onward, wrapping if needed
					// ..or lie about it 
					byte[] recvbuf = new byte[RSR];
					W5100_read((short)(Registers.SOCKET_RX_BASES[socket]+off), recvbuf);
					if (socket == 0) { // MAC RAW socket
						headerlen = 2; // 2 = W5100 MAC RAW header size
						EthernetFrame newframe = new EthernetFrame(recvbuf);
						Log.d("MACRAW In", newframe.toString());
						appLog(newframe.toHeaderString());
						Log.d("MACRAW debug", Util.toHex(recvbuf));
					} else if (socket == 1 || socket == 2) {
						headerlen = 8;  // 8 = W5100 UDP header size
						DHCPPacket newframe = new DHCPPacket(recvbuf);
						Log.d("DHCP In", newframe.toString());
						appLog(newframe.toHeaderString());
						
						Message msg = Message.obtain();
						msg.what = 2;
						msg.obj = newframe;
						try {
							messenger.send(msg);
						} catch (RemoteException e) {
							Log.e(getClass().getName(), "Exception sending DHCP message", e);
							e.printStackTrace();
						}
					} else { // or UDP
						headerlen = 8;  // 8 = W5100 UDP header size
						UDPPacket newframe = new UDPPacket(recvbuf);
						Log.d("UDP In", newframe.toString());
						appLog(newframe.toHeaderString());
					}
					// Tell the W5100 we got it
					W5100_write(SOCKET_REG(socket,Registers.S_RX_RD0), (short) (RD+RSR+headerlen));
					W5100_write(SOCKET_REG(socket,Registers.S_CR), Registers.S_CR_RECV);
					while (W5100_read8(SOCKET_REG(socket,Registers.S_CR)) != (byte) 0) {};
				}
				if ((S_IR & Registers.S_IR_TIMEOUT) != 0) {
					events.append("(TIMEOUT)");
				}
				if ((S_IR & Registers.S_IR_SEND_OK) != 0) {
					events.append("(SEND_OK)");
				}
				appLog(events.toString());
				// clear the interrupts
				W5100_write(SOCKET_REG(socket,Registers.S_IR),S_IR);
			}

			private void appLog(String logline) {
				Message msg = Message.obtain();
				msg.what = 1;
				msg.obj = logline;
				try {
					messenger.send(msg);
				} catch (RemoteException e) {
					Log.e(getClass().getName(), "Exception sending message", e);
					e.printStackTrace();
				}
				
			}

			private byte W5100_read8(short addr) throws ConnectionLostException, InterruptedException {
				byte[] cmdbuf = new byte[4];
				byte[] respbuf = new byte[1];
	
				cmdbuf[0] = 0x0F; // Read command
				cmdbuf[1] = (byte) ((addr & 0xFF00) >> 8);
				cmdbuf[2] = (byte) (addr & 0x00FF);
				cmdbuf[3] = 0; // no data for a read
				
				spi.writeRead(cmdbuf, 3, 4, respbuf, 1);
				//Log.d("W5100_read8", "Read " + Util.toHex(respbuf[0]) + " from "+Util.toHex(addr));
				return respbuf[0];
			}
			private short W5100_read16(short addr) throws ConnectionLostException, InterruptedException {
				byte[] resultbuf = new byte[2];
				byte[] resultbuf2 = new byte[2];
				ByteBuffer bb = ByteBuffer.wrap(resultbuf).order(ByteOrder.BIG_ENDIAN);
				ByteBuffer bb2 = ByteBuffer.wrap(resultbuf2).order(ByteOrder.BIG_ENDIAN);
				
				do {
				resultbuf[0] = W5100_read8(addr);
				resultbuf[1] = W5100_read8((short) (addr+1));
				
				resultbuf2[0] = W5100_read8(addr);
				resultbuf2[1] = W5100_read8((short) (addr+1));
				} while (bb.getShort(0) != bb2.getShort(0));
				return bb.getShort(0);
			}
			private void W5100_read(short addr, byte[] dest) throws ConnectionLostException, InterruptedException {
				byte[] cmdbuf = new byte[4];
				byte[] respbuf = new byte[1];
				ByteBuffer cmdbb = ByteBuffer.wrap(cmdbuf).order(ByteOrder.BIG_ENDIAN);
				ByteBuffer destbb = ByteBuffer.wrap(dest).order(ByteOrder.BIG_ENDIAN);
				
				// TODO: Handle wrap arounds on the buffer
				for (short off=0;off<dest.length;off++) {
					cmdbb.clear();
					cmdbb.put(Registers.READ_CMD);
					cmdbb.putShort((short) (addr+off));
					spi.writeRead(cmdbuf, cmdbuf.length, cmdbuf.length, respbuf, respbuf.length);
					destbb.put(respbuf[0]);
					//dest[off] = respbuf[0];
					//logByteArray("W5100_write(resp)", respbuf);
				}
				Log.d("W5100_read(short,byte[])", "Read " + dest.length + " bytes starting from " + Util.toHex(addr));
//				Log.d("W5100_read(short,byte[])", toHex(dest));
			}
			private void W5100_write(short addr, byte[] data) throws ConnectionLostException, InterruptedException {
				byte[] cmdbuf = new byte[4];
				byte[] respbuf = new byte[4];
				ByteBuffer cmdbb = ByteBuffer.wrap(cmdbuf).order(ByteOrder.BIG_ENDIAN);
	
				// if we're going to overflow the address space something is wrong
				if ((data.length+addr) > 0xFFFF) { throw new IndexOutOfBoundsException(); };
				
				for (short off=0;off<data.length;off++) {
					cmdbb.clear();
					cmdbb.put(Registers.WRITE_CMD);
					cmdbb.putShort((short) (addr+off));
					cmdbb.put(data[off]);
					spi.writeRead(cmdbuf, cmdbuf.length, cmdbuf.length, respbuf, respbuf.length);
					//logByteArray("W5100_write(cmd)", cmdbuf);
					//logByteArray("W5100_write(resp)", respbuf);
				}
				Log.d("W5100_write(short,byte[])", "Wrote " + data.length + " bytes starting from " + Util.toHex(addr));
			}
			private void W5100_write(short addr, byte data) throws ConnectionLostException, InterruptedException {
				W5100_write(addr,new byte[] {data});
			}
			private void W5100_write(short addr, short data) throws ConnectionLostException, InterruptedException {
				byte[] dataa = new byte[2];
				ByteBuffer bb = ByteBuffer.wrap(dataa).order(ByteOrder.BIG_ENDIAN);
				bb.putShort(data);
				W5100_write(addr,dataa);
			}
			private void W5100_write(short addr, int data) throws ConnectionLostException, InterruptedException {
				byte[] dataa = new byte[4];
				ByteBuffer bb = ByteBuffer.wrap(dataa).order(ByteOrder.BIG_ENDIAN);
				bb.putInt(data);
				W5100_write(addr,dataa);
			}
			private void logByteArray(String tag, byte[] bytes) {
				StringBuilder sb = new StringBuilder();
				//Log.e(tag,Hex.encodeHexString(bytes));
				for (byte b: bytes) {
					sb.append(String.format("%02x", b));
				}
				Log.e(tag,sb.toString());
			}

			private short SOCKET_REG(int socket,int register) {return (short) (Registers.SOCKET_CMD_BASES[socket]+register);}
			
			// MAC socket can only be socket 0
			private void setupMACSocket() throws ConnectionLostException, InterruptedException {
				byte statusreg = 0;
				while (statusreg != Registers.S_SR_MACRAW) {
					// Set the socket mode
					W5100_write(SOCKET_REG(0,Registers.S_MR), Registers.S_P_MACRAW);
					W5100_write(SOCKET_REG(0,Registers.S_RX_RD0), (short) Registers.SOCKET_RX_BASES[0]);
					W5100_write(SOCKET_REG(0,Registers.S_CR),Registers.S_CR_OPEN);
					statusreg = W5100_read8(SOCKET_REG(0,Registers.S_SR));
				}
				
			}
			public void setupUDPSocket(int socket, short port) throws ConnectionLostException, InterruptedException {
				byte statusreg = 0;
				while (statusreg != Registers.S_SR_UDP) {
					// Set the socket mode
					W5100_write(SOCKET_REG(socket,Registers.S_MR), Registers.S_P_UDP);
					W5100_write(SOCKET_REG(socket,Registers.S_PORT0),port);
					W5100_write(SOCKET_REG(0,Registers.S_RX_RD0), (short) Registers.SOCKET_RX_BASES[socket]);
					W5100_write(SOCKET_REG(socket,Registers.S_CR),Registers.S_CR_OPEN);
					statusreg = W5100_read8(SOCKET_REG(socket,Registers.S_SR));
				}
				
			}
		    public void sendUDP(int socket, IPv4Address dst, short port, byte[] payload) throws ConnectionLostException, InterruptedException{
		    	W5100_write(SOCKET_REG(socket,(int)Registers.S_DIPR0), dst.addr);
		    	W5100_write(SOCKET_REG(socket,(int)Registers.S_DPORT0), port);
		    	
		    	short off  = (short) (W5100_read16(SOCKET_REG(socket,(int)Registers.S_TX_WR0)) & Registers.SOCKET_TX_MASKS[socket]);
		    	short base = (short) (Registers.SOCKET_TX_BASES[socket]);
		    	
		    	// TODO: Split into multiple writes if we wrap the buffer
		    	// For now, just overflow and hope nothing catches fire
		    	W5100_write((short) (base+off), payload);
		    	
		    	W5100_write(SOCKET_REG(socket,(int)Registers.S_TX_WR0), (short)(off+payload.length));
		    	
		    	// send command
		    	W5100_write(SOCKET_REG(socket,(int)Registers.S_CR), Registers.S_CR_SEND);
		    	
		    	short statusreg = 0xff;
		    	// loop until the command completes
		    	while (statusreg != 0x00) {
		    		statusreg = W5100_read8(SOCKET_REG(socket,Registers.S_CR));
		    	}
		    }
		};
	}
};

