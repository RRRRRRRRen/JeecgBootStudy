package org.jeecg.common.exception;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
/**
 * * Sentinel 是阿里巴巴开源的流量控制组件，常用于限流、熔断、降级。
 */
import org.jeecg.common.constant.enums.ClientTerminalTypeEnum;
import org.jeecg.common.enums.SentinelErrorInfoEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.BrowserUtils;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.springframework.beans.BeansException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * * Spring 在寻找匹配的 @ExceptionHandler 时，遵循下面的逻辑：
 * 
 * * 1.	找最具体的匹配（例如 NullPointerException 会优先找 @ExceptionHandler(NullPointerException.class)）
 * * 2.	如果找不到具体匹配的，就向上查找父类异常（如 Exception.class、Throwable.class）
 * * 3.	找到第一个类型匹配最合适的处理方法，执行该方法，不会继续往下找其他的
 */
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * * 异常处理器
 * 
 * * spring boot 会自动扫描 @RestControllerAdvice 注释
 * * - 当扫描到后会自动全局生效
 * 
 * @Author scott
 * @Date 2019
 */
@RestControllerAdvice
@Slf4j
public class JeecgBootExceptionHandler {

	@Resource
	BaseCommonService baseCommonService;

	/**
	 * * 参数校验错误 处理
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<?> handleValidationExceptions(MethodArgumentNotValidException e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		/**
		 * * 固定写法
		 * 
		 * * getBindingResult() 获取所有校验结果
		 * * getAllErrors() 得到所有错误对象（字段 + 全局）
		 * * .map(ObjectError::getDefaultMessage) 取出每个错误的提示信息
		 * * .collect(Collectors.joining(",")) 用逗号拼接成一个字符串
		 */
		return Result.error("校验失败！" + e.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage)
				.collect(Collectors.joining(",")));
	}

	/**
	 * * 处理自定义异常
	 */
	@ExceptionHandler(JeecgBootException.class)
	public Result<?> handleJeecgBootException(JeecgBootException e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error(e.getErrCode(), e.getMessage());
	}

	/**
	 * * 处理 业务提醒异常
	 */
	@ExceptionHandler(JeecgBootBizTipException.class)
	public Result<?> handleJeecgBootBizTipException(JeecgBootBizTipException e) {
		log.error(e.getMessage());
		return Result.error(e.getErrCode(), e.getMessage());
	}

	/**
	 * * 处理自定义微服务异常
	 */
	@ExceptionHandler(JeecgCloudException.class)
	public Result<?> handleJeecgCloudException(JeecgCloudException e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error(e.getMessage());
	}

	/**
	 * * 处理 401 异常
	 * 
	 * * - 返回 401 http 状态码
	 */
	@ExceptionHandler(JeecgBoot401Exception.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Result<?> handleJeecgBoot401Exception(JeecgBoot401Exception e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return new Result<>(401, e.getMessage());
	}

	/**
	 * * 处理 404 异常
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	public Result<?> handlerNoFoundException(Exception e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error(404, "路径不存在，请检查路径是否正确");
	}

	/**
	 * * 数据库唯一约束 异常
	 * 
	 * * - 由数据库抛出的异常
	 * * - 违反了数据库中某个字段的唯一约束
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(DuplicateKeyException.class)
	public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error("数据库中已存在该记录");
	}

	/**
	 * * Apache Shiro 权限 异常
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler({ UnauthorizedException.class, AuthorizationException.class })
	public Result<?> handleAuthorizationException(AuthorizationException e) {
		log.error(e.getMessage(), e);
		return Result.noauth("没有权限，请联系管理员分配权限！");
	}

	/**
	 * * 兜底异常 处理
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public Result<?> handleException(Exception e) {
		log.error(e.getMessage(), e);
		// * 处理Sentinel限流自定义异常
		Throwable throwable = e.getCause();
		SentinelErrorInfoEnum errorInfoEnum = SentinelErrorInfoEnum.getErrorByException(throwable);
		if (ObjectUtil.isNotEmpty(errorInfoEnum)) {
			return Result.error(errorInfoEnum.getError());
		}
		addSysLog(e);
		return Result.error("操作失败，" + e.getMessage());
	}

	/**
	 * * 请求方式异常 处理
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Result<?> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
		StringBuffer sb = new StringBuffer();
		sb.append("不支持");
		sb.append(e.getMethod());
		sb.append("请求方法，");
		sb.append("支持以下");
		String[] methods = e.getSupportedMethods();
		if (methods != null) {
			for (String str : methods) {
				sb.append(str);
				sb.append("、");
			}
		}
		log.error(sb.toString(), e);
		addSysLog(e);
		// * 405 Method Not Allowed
		return Result.error(405, sb.toString());
	}

	/**
	 * * 超出文件上传大小 处理
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		/**
		 * * 可以通过注入的方式读取xml的配置
		 */
		return Result.error("文件大小超出10MB限制, 请压缩或降低文件质量! ");
	}

	/**
	 * * 数据库唯一约束 异常
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error("执行数据库异常,违反了完整性例如：违反惟一约束、违反非空限制、字段内容超出长度等");
	}

	/**
	 * * Redis 连接异常
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(PoolException.class)
	public Result<?> handlePoolException(PoolException e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error("Redis 连接异常!");
	}

	/**
	 * * SQL注入风险，全局异常处理
	 *
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(JeecgSqlInjectionException.class)
	public Result<?> handleSQLException(Exception exception) {
		String msg = exception.getMessage().toLowerCase();
		final String extractvalue = "extractvalue";
		final String updatexml = "updatexml";
		boolean hasSensitiveInformation = msg.indexOf(extractvalue) >= 0 || msg.indexOf(updatexml) >= 0;
		if (msg != null && hasSensitiveInformation) {
			log.error("校验失败，存在SQL注入风险！{}", msg);
			return Result.error("校验失败，存在SQL注入风险！");
		}
		addSysLog(exception);
		return Result.error("校验失败，存在SQL注入风险！" + msg);
	}

	/**
	 * * 添加异常新系统日志
	 * 
	 * @param e 异常
	 * @author chenrui
	 * @date 2024/4/22 17:16
	 */
	private void addSysLog(Throwable e) {
		// * 异常日志初始化
		LogDTO log = new LogDTO();
		log.setLogType(CommonConstant.LOG_TYPE_4);
		log.setLogContent(e.getClass().getName() + ":" + e.getMessage());
		log.setRequestParam(ExceptionUtils.getStackTrace(e));

		// * 获取request
		HttpServletRequest request = null;
		try {
			request = SpringContextUtils.getHttpServletRequest();
		} catch (NullPointerException | BeansException ignored) {
		}

		if (null != request) {
			// * 请求的参数
			Map<String, String[]> parameterMap = request.getParameterMap();
			if (!CollectionUtils.isEmpty(parameterMap)) {
				log.setMethod(oConvertUtils.mapToString(request.getParameterMap()));
			}
			// * 请求地址
			log.setRequestUrl(request.getRequestURI());
			// * 设置IP地址
			log.setIp(IpUtils.getIpAddr(request));
			// * 设置客户端
			if (BrowserUtils.isDesktop(request)) {
				log.setClientType(ClientTerminalTypeEnum.PC.getKey());
			} else {
				log.setClientType(ClientTerminalTypeEnum.APP.getKey());
			}
		}

		// * 获取登录用户信息
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (sysUser != null) {
			log.setUserid(sysUser.getUsername());
			log.setUsername(sysUser.getRealname());

		}
		baseCommonService.addLog(log);
	}

}
