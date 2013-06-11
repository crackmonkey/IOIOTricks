package org.glowingmonkey.ioiotricks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.util.Log;

public class Util {
	public static void logByteArray(String tag, byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		//Log.e(tag,Hex.encodeHexString(bytes));
		for (byte b: bytes) {
			sb.append(String.format("%02x", b));
		}
		Log.e(tag,sb.toString());
	}
	public static String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b: bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
	public static String toHex(short data) {
		return toHex(toBytes(data));
	}
	public static String toHex(int data) {
		return toHex(toBytes(data));
	}
	public static byte[] toBytes(short data) {
		byte[] dataa = new byte[2];
		ByteBuffer bb = ByteBuffer.wrap(dataa).order(ByteOrder.BIG_ENDIAN);
		bb.putShort(data);
		return dataa;
	}
	public static byte[] toBytes(int data) {
		byte[] dataa = new byte[4];
		ByteBuffer bb = ByteBuffer.wrap(dataa).order(ByteOrder.BIG_ENDIAN);
		bb.putInt(data);
		return dataa;
	}
}
