package org.jeecg.common.util;

import java.security.MessageDigest;

/**
 * @Description: 加密工具
 * @author: jeecg-boot
 */
public class Md5Util {

	/**
	 * * 十六进制字符数组，用于查表转换
	 */
	private static final String[] HEXDIGITS = {
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"
	};

	/**
	 * * 将字节数组转换为十六进制字符串。
	 *
	 * @param b 要转换的字节数组（通常是 MessageDigest 输出的 MD5 值）
	 * @return 十六进制字符串表示
	 */
	public static String byteArrayToHexString(byte[] b) {
		/**
		 * *使用 StringBuffer 构造结果字符串
		 * 
		 * * StringBuffer 是线程安全的，适合多线程环境
		 * * 如果没有并发需求，可以使用 StringBuilder 替代以提升性能
		 */
		StringBuffer resultSb = new StringBuffer();

		/**
		 * * 遍历每一个字节，将每个字节转换为两个十六进制字符并追加到结果中
		 */
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}

		// * 返回完整的十六进制字符串
		return resultSb.toString();
	}

	/**
	 * * 将一个字节（8 位）转换为对应的两位十六进制字符串。
	 *
	 * @param b 要转换的单个字节
	 * @return 两位十六进制字符串（如 "0A"、"FF" 等）
	 */
	private static String byteToHexString(byte b) {
		int n = b;

		// * 如果字节为负数（Java 中 byte 是有符号的），将其转为对应的无符号整数（0~255）
		if (n < 0) {
			n += 256;
		}

		/**
		 * * 计算字节的二进制表示的十六进制数 00000000 => 0000,0000
		 * 
		 * * 计算高四位对应的十六进制数值
		 * * 计算低四位对应的十六进制数值
		 */
		int d1 = n / 16;
		int d2 = n % 16;

		// * 从预定义的十六进制字符数组中取出对应字符拼接为字符串
		return HEXDIGITS[d1] + HEXDIGITS[d2];
	}

	/**
	 * * 对给定的字符串进行 MD5 编码，并以十六进制字符串形式返回。
	 *
	 * @param origin      原始字符串
	 * @param charsetname 字符编码（如 "UTF-8"）；如果为 null 或空字符串，则使用平台默认编码
	 * @return 编码后的十六进制字符串，如果发生异常则返回 null
	 */
	public static String md5Encode(String origin, String charsetname) {
		String resultString = null;
		try {
			// * 创建字符串副本（此操作其实多余，origin 本身就是 String 类型）
			resultString = new String(origin);

			// * 获取 MD5 摘要算法的实例
			MessageDigest md = MessageDigest.getInstance("MD5");

			/**
			 * * 判断是否指定字符编码
			 * 
			 * * 如果未指定编码，使用平台默认字符集将字符串转换为字节数组，再进行 MD5 编码
			 * * 如果指定了编码，使用指定的字符集进行转换
			 */
			if (charsetname == null || "".equals(charsetname)) {
				resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
			} else {
				resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
			}
		} catch (Exception exception) {
			// * 异常被捕获但未处理，可能隐藏问题（建议至少打印日志）
		}
		// * 返回 MD5 编码结果（十六进制字符串）
		return resultString;
	}

}
