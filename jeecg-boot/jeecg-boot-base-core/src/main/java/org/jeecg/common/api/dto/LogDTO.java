package org.jeecg.common.api.dto;

import lombok.Data;
import org.jeecg.common.system.vo.LoginUser;
import java.io.Serializable;
import java.util.Date;

/**
 * * 日志对象
 */
@Data
public class LogDTO implements Serializable {

    /**
     * * 序列化ID
     */
    private static final long serialVersionUID = 8482720462943906924L;

    /**
     * * 日志内容
     */
    private String logContent;

    /**
     * * 日志类型
     * * (0:操作日志;1:登录日志;2:定时任务)
     */
    private Integer logType;

    /**
     * * 操作类型
     * * (1:添加;2:修改;3:删除;)
     */
    private Integer operateType;

    /**
     * * 登录用户
     */
    private LoginUser loginUser;

    /**
     * * 主键ID
     */
    private String id;

    /**
     * * 创建人
     */
    private String createBy;

    /**
     * * 创建时间
     */
    private Date createTime;

    /**
     * * 消耗时间
     */
    private Long costTime;

    /**
     * * 请求ip
     */
    private String ip;

    /**
     * * 请求参数
     */
    private String requestParam;

    /**
     * * 请求类型
     */
    private String requestType;

    /**
     * * 请求路径
     */
    private String requestUrl;

    /**
     * * 请求方法
     */
    private String method;

    /**
     * * 操作人用户名称
     */
    private String username;

    /**
     * * 操作人用户id
     */
    private String userid;

    /**
     * * 租户ID
     */
    private Integer tenantId;

    /**
     * * 客户终端类型
     * * pc:电脑端 app:手机端 h5:移动网页端
     */
    private String clientType;

    /**
     * * 无参构造器
     */
    public LogDTO() {
    }

    /**
     * * 带参构造器
     *
     * @param logContent  日志内容
     * @param logType     日志类型
     * @param operatetype 操作类型
     */
    public LogDTO(String logContent, Integer logType, Integer operatetype) {
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
    }

    /**
     * * 带参构造器
     *
     * @param logContent  日志内容
     * @param logType     日志类型
     * @param operatetype 操作类型
     * @param loginUser   用户
     */
    public LogDTO(String logContent, Integer logType, Integer operatetype, LoginUser loginUser) {
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
        this.loginUser = loginUser;
    }
}
