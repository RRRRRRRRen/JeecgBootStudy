package org.jeecg.common.util;

import java.net.InetAddress;

/**
 * * UUID
 * 
 * @Author 张代浩
 *
 */
public class UUIDGenerator {

	/**
	 * * 产生一个32位的UUID
	 * 
	 * @return
	 */
	public static String generate() {
		return new StringBuilder(32).append(format(getIp())).append(
				format(getJvm())).append(format(getHiTime())).append(
						format(getLoTime()))
				.append(format(getCount())).toString();

	}

	/**
	 * * 转为整数的ip
	 */
	private static final int IP;

	/**
	 * * 初始化ip为整数
	 */
	static {
		int ipadd;
		try {
			ipadd = toInt(InetAddress.getLocalHost().getAddress());
		} catch (Exception e) {
			ipadd = 0;
		}
		IP = ipadd;
	}

	/**
	 * * 返回转化为整数的ip
	 */
	private final static int getIp() {
		return IP;
	}

	/**
	 * * 32位转16进制
	 * 
	 * @param intval
	 * @return
	 */
	private final static String format(int intval) {
		String formatted = Integer.toHexString(intval);
		StringBuilder buf = new StringBuilder("00000000");
		buf.replace(8 - formatted.length(), 8, formatted);
		return buf.toString();
	}

	/**
	 * * 16位转16进制
	 * 
	 * @param intval
	 * @return
	 */
	private final static String format(short shortval) {
		String formatted = Integer.toHexString(shortval);
		StringBuilder buf = new StringBuilder("0000");
		buf.replace(4 - formatted.length(), 4, formatted);
		return buf.toString();
	}

	/**
	 * * 减少 8 时间戳精度
	 */
	private static final int JVM = (int) (System.currentTimeMillis() >>> 8);

	/**
	 * * 返回减少精度的时间戳
	 * 
	 * @return
	 */
	private final static int getJvm() {
		return JVM;
	}

	private static short counter = (short) 0;

	private final static short getCount() {
		synchronized (UUIDGenerator.class) {
			if (counter < 0) {
				counter = 0;
			}
			return counter++;
		}
	}

	/**
	 * * 减少 32 时间戳精度
	 */
	private final static short getHiTime() {
		return (short) (System.currentTimeMillis() >>> 32);
	}

	/**
	 * * 时间戳
	 */
	private final static int getLoTime() {
		return (int) System.currentTimeMillis();
	}

	/**
	 * * ip 地址转化为整数
	 * 
	 * @param bytes
	 * @return
	 */
	private final static int toInt(byte[] bytes) {
		int result = 0;
		int length = 4;
		for (int i = 0; i < length; i++) {
			result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
		}
		return result;
	}

}
