package org.jeecg.common.exception;

import org.jeecg.common.constant.CommonConstant;

/**
 * * jeecg-boot自定义异常
 * 
 * @Description: jeecg-boot自定义异常
 * @author: jeecg-boot
 */
public class JeecgBootException extends RuntimeException {
	/**
	 * * 序列化ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * * 返回服务器异常 code 500
	 */
	private int errCode = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;

	/**
	 * * 获取错误码
	 * 
	 * @return 错误码
	 */
	public int getErrCode() {
		return errCode;
	}

	/**
	 * * 设置错误信息
	 * 
	 * @param message 错误信息
	 */
	public JeecgBootException(String message) {
		super(message);
	}

	/**
	 * * 设置错误信息和错误码
	 * 
	 * @param message 错误信息
	 * @param errCode 错误码
	 */
	public JeecgBootException(String message, int errCode) {
		super(message);
		this.errCode = errCode;
	}

	/**
	 * * 设置异常原因
	 * 
	 * @param cause 异常原因
	 */
	public JeecgBootException(Throwable cause) {
		super(cause);
	}

	/**
	 * * 设置错误信息和异常原因
	 * 
	 * @param message 错误信息
	 * @param cause   异常原因
	 */
	public JeecgBootException(String message, Throwable cause) {
		super(message, cause);
	}
}
