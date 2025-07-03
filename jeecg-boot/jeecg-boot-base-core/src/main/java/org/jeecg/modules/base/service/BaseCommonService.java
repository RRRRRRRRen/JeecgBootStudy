package org.jeecg.modules.base.service;

import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.system.vo.LoginUser;

/**
 * * 日志接口
 * 
 * @author: jeecg-boot
 */
public interface BaseCommonService {

    /**
     * * 保存日志
     * 
     * @param logDTO 日志DTO
     */
    void addLog(LogDTO logDTO);

    /**
     * * 保存日志
     * 
     * @param logContent  日志内容
     * @param logType     日志类型
     * @param operateType 操作类型
     * @param user        登录用户
     */
    void addLog(String logContent, Integer logType, Integer operateType, LoginUser user);

    /**
     * * 保存日志
     * 
     * @param logContent  日志内容
     * @param logType     日志类型
     * @param operateType 操作类型
     */
    void addLog(String logContent, Integer logType, Integer operateType);

}
