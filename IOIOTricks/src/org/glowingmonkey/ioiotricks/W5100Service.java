package org.glowingmonkey.ioiotricks;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalInput.Spec.Mode;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.SpiMaster;
import ioio.lib.api.SpiMaster.Rate;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.codec.android.binary.Hex;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class W5100Service extends IOIOService {
	protected static final String LOG_MESSAGE = "org.glowingmonkey.ioiotricks.action.LOG";
	protected static final String LOG_TEXT = "org.glowingmonkey.ioiotricks.fondler.LOGLINE";
	Messenger messenger;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Bundle extras = intent.getExtras();
		messenger = (Messenger) extras.get("MESSENGER");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			private SpiMaster spi;
			private byte[] readaddr = new byte[] {10};
			private ByteBuffer bytebuf;
			
			static final int misoPin = 30;
			static final int mosiPin = 31;
			static final int clkPin = 32;
			int ssPin = 33;

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
				//spi = ioio_.openSpiMaster(37, 38, 39, 40, SpiMaster.Rate.RATE_125K);
				spi = ioio_.openSpiMaster(new DigitalInput.Spec(misoPin,
						Mode.PULL_UP), new DigitalOutput.Spec(mosiPin),
						new DigitalOutput.Spec(clkPin),
						new DigitalOutput.Spec[] { new DigitalOutput.Spec(ssPin) },
						new SpiMaster.Config(Rate.RATE_1M, false, false));
				
				// Reset the chip
				W5100_write(Registers.MR, (byte) Registers.MR_RST);
				W5100_write(Registers.IMR, (byte) 0xdf);
				W5100_write(Registers.RTR0, new byte[] {0x07,(byte) 0xd0});
				//W5100_write(Registers.RTR1, (byte) 0xd0);
				W5100_write(Registers.RCR, (byte) 0x08);
				
				// set a MAC
				W5100_write(Registers.SIPR0, new byte[] {(byte) 0xde,(byte) 0xad,(byte) 0xbe,(byte) 0xef,0,1});
				
				// set an IP
				W5100_write(Registers.SIPR0, new byte[] {10,20,30,44});
				// netmask
				W5100_write(Registers.SUBR0, new byte[] {10,20,30,44});
				W5100_write(Registers.SUBR0, new byte[] {(byte)255,(byte)255,(byte)255,0});
				// Gateway
				W5100_write(Registers.GAR0, new byte[] {10,20,30,1});
				
				// Set buffer sizes, 2k to each socket
				W5100_write(Registers.TMSR,(byte)0x55);
				W5100_write(Registers.RMSR,(byte)0x55);
			};

			@Override
			public void loop() throws ConnectionLostException,
					InterruptedException {
				appLog("W5100 MR: 0b"+Integer.toBinaryString(W5100_read8(Registers.MR)));
				appLog("W5100 IR: 0b"+Integer.toBinaryString(W5100_read8(Registers.IR)));
				appLog("W5100 TMSR: 0x"+Integer.toHexString(W5100_read8(Registers.TMSR)));
				appLog("W5100 GAR0: 0x"+Integer.toHexString(W5100_read8(Registers.GAR0)));
				Thread.sleep(1000);
			}

			private void appLog(String logline) {
				Message msg = Message.obtain();
				msg.what = 1;
				try {
					messenger.send(msg);
				} catch (RemoteException e) {
					Log.e(getClass().getName(), "Exception sending message", e);
					e.printStackTrace();
				}
				msg.obj = logline;
			}

			public byte W5100_read8(short addr) throws ConnectionLostException, InterruptedException {
				byte[] cmdbuf = new byte[4];
				byte[] respbuf = new byte[4];
	
				cmdbuf[0] = 0x0F; // Read command
				cmdbuf[1] = (byte) ((addr & 0xFF00) >> 8);
				cmdbuf[2] = (byte) (addr & 0x00FF);
				cmdbuf[3] = 0; // no data for a read
				
				logByteArray("W5100_read8(cmd)", cmdbuf);
				
				spi.writeRead(cmdbuf, 3, 4, respbuf, 1);
				logByteArray("W5100_read8(resp)", respbuf);
				return respbuf[0];
			}
			public void W5100_write(short addr, byte[] data) throws ConnectionLostException, InterruptedException {
				byte[] cmdbuf = new byte[4];
				byte[] respbuf = new byte[4];
				ByteBuffer cmdbb = ByteBuffer.wrap(cmdbuf).order(ByteOrder.BIG_ENDIAN);
	
				// if we're going to overflow the address space something is wrong
				if ((data.length+addr) > Short.MAX_VALUE) { throw new IndexOutOfBoundsException(); };
				
				for (short off=0;off<data.length;off++) {
					cmdbb.clear();
					cmdbb.put(Registers.WRITE_CMD);
					cmdbb.putShort((short) (addr+off));
					cmdbb.put(data[off]);
					spi.writeRead(cmdbuf, cmdbuf.length, cmdbuf.length, respbuf, respbuf.length);
					logByteArray("W5100_write(cmd)", cmdbuf);
					//logByteArray("W5100_write(resp)", respbuf);
				}
			}
			public void W5100_write(short addr, byte data) throws ConnectionLostException, InterruptedException {
				W5100_write(addr,new byte[] {data});
			}
			public void W5100_write(short addr, short data) throws ConnectionLostException, InterruptedException {
				byte[] dataa = new byte[2];
				ByteBuffer bb = ByteBuffer.wrap(dataa).order(ByteOrder.BIG_ENDIAN);
				bb.putShort(data);
				W5100_write(addr,dataa);
			}
			public void W5100_write(short addr, int data) throws ConnectionLostException, InterruptedException {
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
		};
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
};

