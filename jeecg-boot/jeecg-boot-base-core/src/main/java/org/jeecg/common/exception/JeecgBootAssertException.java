package org.jeecg.common.exception;

/**
 * * jeecgboot断言异常
 * 
 * @author chenrui
 * @date 2025/2/14 14:31
 */
public class JeecgBootAssertException extends JeecgBootException {
	private static final long serialVersionUID = 1L;


	/**
	 * * 设置错误信息
	 * 
	 * @param message 错误信息
	 */
	public JeecgBootAssertException(String message) {
		super(message);
	}

	/**
	 * * 设置错误信息和错误码
	 *
	 * @param message 错误信息
	 * @param errCode 错误码
	 */
	public JeecgBootAssertException(String message, int errCode) {
		super(message, errCode);
	}

}
