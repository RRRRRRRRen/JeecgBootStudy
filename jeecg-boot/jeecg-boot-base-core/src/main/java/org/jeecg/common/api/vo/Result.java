package org.jeecg.common.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.constant.CommonConstant;

import java.io.Serializable;

/**
 * 接口返回数据格式
 * 
 * @author scott
 * @email jeecgos@163.com
 * @date 2019年1月19日
 */
@Data
@Schema(description = "接口返回对象")
public class Result<T> implements Serializable {

	/**
	 * * 序列化ID
	 * * 作用：
	 * * 1. 确保在反序列化时，接收方能够识别发送方的类版本。
	 * * 2. 如果发送方的类版本与接收方的类版本不一致，Java会根据serialVersionUID来判断是否可以进行反序列化。
	 * * - 如果serialVersionUID不匹配，Java会抛出InvalidClassException异常，表示类版本不兼容。
	 * * - 如果serialVersionUID匹配，Java会继续进行反序列化过程，将字节流转换为对象。
	 * * 3. 如果没有显式声明serialVersionUID，Java会根据类的结构自动生成一个默认的serialVersionUID。
	 * * 4. 显式声明serialVersionUID可以避免在类结构发生变化时，自动生成的serialVersionUID发生变化，从而导致反序列化失败。
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * * 成功标志
	 */
	@Schema(description = "成功标志")
	private boolean success = true;

	/**
	 * * 返回处理消息
	 */
	@Schema(description = "返回处理消息")
	private String message = "";

	/**
	 * * 返回代码
	 */
	@Schema(description = "返回代码")
	private Integer code = 0;

	/**
	 * * 返回数据对象 data
	 */
	@Schema(description = "返回数据对象")
	private T result;

	/**
	 * * 时间戳
	 */
	@Schema(description = "时间戳")
	private long timestamp = System.currentTimeMillis();

	/**
	 * * 无参构造方法
	 */
	public Result() {
	}

	/**
	 * * 有参构造方法
	 * 
	 * @param code
	 * @param message
	 */
	public Result(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * * 成功返回
	 * 
	 * @param message
	 * @return Result对象
	 */
	public Result<T> success(String message) {
		this.message = message;
		this.code = CommonConstant.SC_OK_200;
		this.success = true;
		return this;
	}

	/**
	 * * 失败返回
	 * 
	 * @param msg 操作成功的消息
	 * @return Result对象
	 */
	public Result<T> error500(String message) {
		this.message = message;
		this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
		this.success = false;
		return this;
	}

	/**
	 * * 创建一个表示操作成功的Result对象
	 * 
	 * @return Result对象
	 */
	public static <T> Result<T> ok() {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		return r;
	}

	/**
	 * * 创建一个表示操作成功的Result对象
	 * 
	 * @param msg 操作成功的消息
	 * @return Result对象
	 */
	public static <T> Result<T> ok(String msg) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		r.setResult((T) msg);
		r.setMessage(msg);
		return r;
	}

	/**
	 * * 创建一个表示操作成功的Result对象
	 * 
	 * @return Result对象
	 */
	public static <T> Result<T> ok(T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		r.setResult(data);
		return r;
	}

	/**
	 * * 此方法是为了兼容升级所创建
	 * 
	 * @return Result对象
	 */
	public static <T> Result<T> OK() {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		return r;
	}

	/**
	 * * 此方法是为了兼容升级所创建
	 *
	 * @param msg
	 * @return Result对象
	 */
	public static <T> Result<T> OK(String msg) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		r.setMessage(msg);
		r.setResult((T) msg);
		return r;
	}

	/**
	 * * 此方法是为了兼容升级所创建
	 *
	 * @param msg
	 * @return Result对象
	 */
	public static <T> Result<T> OK(T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		r.setResult(data);
		return r;
	}

	/**
	 * * 创建一个表示操作成功的Result对象
	 * 
	 * @param msg  操作成功的消息
	 * @param data 操作成功结果数据
	 * @return Result对象
	 */
	public static <T> Result<T> OK(String msg, T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(CommonConstant.SC_OK_200);
		r.setMessage(msg);
		r.setResult(data);
		return r;
	}

	/**
	 * * 创建一个表示操作失败的Result对象
	 * 
	 * @param msg  错误消息
	 * @param data 错误数据
	 * @return Result对象
	 */
	public static <T> Result<T> error(String msg, T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(false);
		r.setCode(CommonConstant.SC_INTERNAL_SERVER_ERROR_500);
		r.setMessage(msg);
		r.setResult(data);
		return r;
	}

	/**
	 * * 创建一个表示操作失败的Result对象
	 * 
	 * @param msg 错误消息
	 * @return Result对象
	 */
	public static <T> Result<T> error(String msg) {
		return error(CommonConstant.SC_INTERNAL_SERVER_ERROR_500, msg);
	}

	/**
	 * * 创建一个表示操作失败的Result对象
	 * 
	 * @param code 错误状态码
	 * @param msg  错误消息
	 * @return Result对象
	 */
	public static <T> Result<T> error(int code, String msg) {
		Result<T> r = new Result<T>();
		r.setCode(code);
		r.setMessage(msg);
		r.setSuccess(false);
		return r;
	}

	/**
	 * * 无权限访问返回结果
	 * 
	 * @param msg 错误消息
	 * @return Result对象
	 */
	public static <T> Result<T> noauth(String msg) {
		return error(CommonConstant.SC_JEECG_NO_AUTHZ, msg);
	}

	/**
	 * TODO 未知功能的属性
	 * * 该属性会在序列化过程中忽略
	 */
	@JsonIgnore
	private String onlTable;

}