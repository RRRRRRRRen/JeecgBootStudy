package org.jeecg.common.aspect;

import com.alibaba.fastjson.JSONObject;
/**
 * * PropertyFilter 是 FastJSON 提供的“属性过滤器”接口，用于在序列化对象为 JSON 时按需排除或保留字段。
 */
import com.alibaba.fastjson.serializer.PropertyFilter;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
/**
 * * ProceedingJoinPoint 是 Spring AOP 环绕通知（@Around） 中的一个参数
 * 
 * * - 表示当前被拦截的方法调用的信息，并允许你手动控制方法是否执行、何时执行、执行几次。
 * * - 它是 JoinPoint 的子接口，只能用于 @Around 通知中。
 */
import org.aspectj.lang.ProceedingJoinPoint;
/**
 * * 定义环绕通知，拦截方法前后都能执行逻辑
 */
import org.aspectj.lang.annotation.Around;
/**
 * * 表明这是一个切面类
 */
import org.aspectj.lang.annotation.Aspect;
/**
 * * 定义一个切入点，哪些方法需要拦截
 */
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ModuleType;
import org.jeecg.common.constant.enums.OperateTypeEnum;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
/**
 * * 在运行时获取方法参数的名称
 */
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
/**
 * * 让 Spring 扫描并注册成 Bean
 */
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * * 系统日志，切面处理类
 *
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2018年1月14日
 */
@Aspect
@Component
public class AutoLogAspect {

    @Resource
    private BaseCommonService baseCommonService;

    /**
     * * 1️⃣ 定义切入点：拦截带 @AutoLog 注解的方法
     */
    @Pointcut("@annotation(org.jeecg.common.aspect.annotation.AutoLog)")
    public void logPointCut() {

    }

    /**
     * * 2️⃣ 定义环绕通知：方法执行前后都拦截
     * 
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        // * 执行被代理的方法
        Object result = point.proceed();
        // * 执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        // * 保存日志
        saveSysLog(point, time, result);
        // * 返回被代理修正后的结果
        return result;
    }

    /**
     * * 保存日志
     * 
     * @param joinPoint
     * @param time
     * @param obj
     */
    private void saveSysLog(ProceedingJoinPoint joinPoint, long time, Object obj) {
        /**
         * * 获取方法签名
         * 
         * * - 如果你在非方法切点中使用这个强转，则会报错
         */
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // * 初始化日志对象
        LogDTO dto = new LogDTO();

        AutoLog syslog = method.getAnnotation(AutoLog.class);
        if (syslog != null) {
            // * 获取注解的描述
            String content = syslog.value();
            // * ONLINE 类型特殊处理
            if (syslog.module() == ModuleType.ONLINE) {
                content = getOnlineLogContent(obj, content);
            }
            // * 注解上的描述,操作日志内容
            dto.setLogType(syslog.logType());
            dto.setLogContent(content);
        }

        // * 设置 请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        dto.setMethod(className + "." + methodName + "()");

        // * 设置操作类型
        if (CommonConstant.LOG_TYPE_2 == dto.getLogType()) {
            dto.setOperateType(getOperateType(methodName, syslog.operateType()));
        }

        // * 获取request
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        // * 请求的参数
        dto.setRequestParam(getReqestParams(request, joinPoint));
        // * 设置IP地址
        dto.setIp(IpUtils.getIpAddr(request));
        // * 获取登录用户信息
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser != null) {
            dto.setUserid(sysUser.getUsername());
            dto.setUsername(sysUser.getRealname());

        }
        // * 耗时
        dto.setCostTime(time);
        dto.setCreateTime(new Date());
        // * 保存系统日志
        baseCommonService.addLog(dto);
    }

    /**
     * * 获取操作类型
     */
    private int getOperateType(String methodName, int operateType) {
        if (operateType > 0) {
            return operateType;
        }
        return OperateTypeEnum.getTypeByMethodName(methodName);
    }

    /**
     * * 获取请求参数
     * 
     * @Description: 获取请求参数
     * @author: scott
     * @date: 2020/4/16 0:10
     * @param request:   request
     * @param joinPoint: joinPoint
     * @Return: java.lang.String
     */
    private String getReqestParams(HttpServletRequest request, JoinPoint joinPoint) {
        String httpMethod = request.getMethod();
        String params = "";

        if (CommonConstant.HTTP_POST.equals(httpMethod) || CommonConstant.HTTP_PUT.equals(httpMethod)
                || CommonConstant.HTTP_PATCH.equals(httpMethod)) {
            // * 获取函数执行参数列表
            Object[] paramsArray = joinPoint.getArgs();
            Object[] arguments = new Object[paramsArray.length];
            for (int i = 0; i < paramsArray.length; i++) {
                // * ServletRequest不能序列化，从入参里排除，否则报异常
                if (paramsArray[i] instanceof BindingResult
                        || paramsArray[i] instanceof ServletRequest
                        || paramsArray[i] instanceof ServletResponse
                        || paramsArray[i] instanceof MultipartFile) {
                    continue;
                }
                arguments[i] = paramsArray[i];
            }

            // * 日志数据太长的直接过滤掉
            PropertyFilter profilter = new PropertyFilter() {
                @Override
                public boolean apply(Object o, String name, Object value) {
                    int length = 500;
                    if (value != null && value.toString().length() > length) {
                        return false;
                    }
                    if (value instanceof MultipartFile) {
                        return false;
                    }
                    return true;
                }
            };
            params = JSONObject.toJSONString(arguments, profilter);
        } else {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();
            // * 请求的方法参数名称
            LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
            String[] paramNames = u.getParameterNames(method);
            if (args != null && paramNames != null) {
                for (int i = 0; i < args.length; i++) {
                    params += "  " + paramNames[i] + ": " + args[i];
                }
            }
        }
        return params;
    }

    /**
     * * online日志内容拼接
     * 
     * @param obj     方法执行返回结果
     * @param content 注解内容
     * @return
     */
    private String getOnlineLogContent(Object obj, String content) {
        // * 判断返回结果是否为 Result 类型
        if (Result.class.isInstance(obj)) {
            Result<?> res = (Result<?>) obj;
            String msg = res.getMessage();
            String tableName = res.getOnlTable();
            if (oConvertUtils.isNotEmpty(tableName)) {
                content += ",表名:" + tableName;
            }
            if (res.isSuccess()) {
                content += "," + (oConvertUtils.isEmpty(msg) ? "操作成功" : msg);
            } else {
                content += "," + (oConvertUtils.isEmpty(msg) ? "操作失败" : msg);
            }
        }
        return content;
    }
}
