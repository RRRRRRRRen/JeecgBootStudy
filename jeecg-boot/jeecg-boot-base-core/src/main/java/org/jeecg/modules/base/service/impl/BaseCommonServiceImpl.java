package org.jeecg.modules.base.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.constant.enums.ClientTerminalTypeEnum;
import org.jeecg.common.util.BrowserUtils;
import org.jeecg.modules.base.mapper.BaseCommonMapper;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * * Log 日志
 * 
 * @Description: common实现类
 * @author: jeecg-boot
 */
@Service
@Slf4j
public class BaseCommonServiceImpl implements BaseCommonService {

    @Resource
    private BaseCommonMapper baseCommonMapper;

    @Override
    public void addLog(LogDTO logDTO) {
        // * 没有id则生成一个id保存
        if (oConvertUtils.isEmpty(logDTO.getId())) {
            logDTO.setId(String.valueOf(IdWorker.getId()));
        }

        // * 异常捕获处理，防止数据太大存储失败，导致业务失败
        try {
            // * 保存日志
            logDTO.setCreateTime(new Date());
            baseCommonMapper.saveLog(logDTO);
        } catch (Exception e) {
            // * 保存失败则采用 Slf4j 保存
            log.warn(" LogContent length : " + logDTO.getLogContent().length());
            log.warn(e.getMessage());
        }
    }

    @Override
    public void addLog(String logContent, Integer logType, Integer operatetype, LoginUser user) {
        // * 读取参数到 LogDTO
        LogDTO sysLog = new LogDTO();
        sysLog.setId(String.valueOf(IdWorker.getId()));
        sysLog.setLogContent(logContent);
        sysLog.setLogType(logType);
        sysLog.setOperateType(operatetype);

        try {
            /**
             * * 获取 HttpServletRequest
             * 
             * * - 在非 Controller 层（比如 Service 或工具类）里，获取当前线程下的 HttpServletRequest 对象。
             */
            HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            // * 设置IP地址
            sysLog.setIp(IpUtils.getIpAddr(request));

            // * 设置客户端
            try {
                if (BrowserUtils.isDesktop(request)) {
                    sysLog.setClientType(ClientTerminalTypeEnum.PC.getKey());
                } else {
                    sysLog.setClientType(ClientTerminalTypeEnum.APP.getKey());
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        } catch (Exception e) {
            // * 如果ip获取失败，则设置为本地
            sysLog.setIp("127.0.0.1");
        }

        // * 获取登录用户信息
        if (user == null) {
            try {
                // * 从 Shiro 的安全上下文中获取当前登录用户对象，并强制转换为 LoginUser 类型。
                user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
        if (user != null) {
            sysLog.setUserid(user.getUsername());
            sysLog.setUsername(user.getRealname());
        }
        sysLog.setCreateTime(new Date());

        // * 保存日志
        try {
            baseCommonMapper.saveLog(sysLog);
        } catch (Exception e) {
            log.warn(" LogContent length : " + sysLog.getLogContent().length());
            log.warn(e.getMessage());
        }
    }

    @Override
    public void addLog(String logContent, Integer logType, Integer operateType) {
        addLog(logContent, logType, operateType, null);
    }

}
