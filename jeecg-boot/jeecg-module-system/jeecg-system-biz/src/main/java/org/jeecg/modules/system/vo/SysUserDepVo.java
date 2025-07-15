package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * * 用户部门
 * 
 * @Author qinfeng
 * @Date 2020/1/2 21:58
 * @Description:
 * @Version 1.0
 */
@Data
public class SysUserDepVo {
    /**
     * * 用户id
     */
    private String userId;

    /**
     * * 部门名称
     */
    private String departName;

    /**
     * * 部门id
     */
    private String deptId;

    /**
     * * 部门的父级id
     */
    private String parentId;
}
