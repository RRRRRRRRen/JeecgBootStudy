package org.jeecg.common.constant.enums;

import org.jeecg.common.util.oConvertUtils;

import java.util.List;

/**
 * * 首页自定义
 * * - 通过角色编码与首页组件路径配置
 * * - 枚举的顺序有权限高低权重作用（也就是配置多个角色，在前面的角色首页，会优先生效）
 * 
 * @author: jeecg-boot
 */
public enum RoleIndexConfigEnum {

    /**
     * * 首页自定义 admin
     */
    // ADMIN("admin", "dashboard/Analysis"),
    // TEST("test", "dashboard/IndexChart"),

    /**
     * * 首页自定义 hr
     */
    // HR("hr", "dashboard/IndexBdc");

    /**
     * * 首页自定义 dm
     */
    // DM("dm", "dashboard/IndexTask"),

    /**
     * * 此值仅为防止报错，无任何实际意义
     */
    ROLE_INDEX_CONFIG_ENUM("RoleIndexConfigEnumDefault", "dashboard/Analysis");

    /**
     * * 角色编码
     */
    String roleCode;
    /**
     * * 路由index
     */
    String componentUrl;

    /**
     * * 构造器
     *
     * @param roleCode     角色编码
     * @param componentUrl 首页组件路径（规则跟菜单配置一样）
     */
    RoleIndexConfigEnum(String roleCode, String componentUrl) {
        this.roleCode = roleCode;
        this.componentUrl = componentUrl;
    }

    /**
     * * 根据 角色 查找 首页路由地址
     * 
     * @param roleCode 角色编码
     * @return
     */
    private static RoleIndexConfigEnum getEnumByCode(String roleCode) {
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (e.roleCode.equals(roleCode)) {
                return e;
            }
        }
        return null;
    }

    /**
     * * 根据 角色 查找 首页路由地址
     * 
     * @param roleCode 角色编码
     * @return
     */
    private static String getIndexByCode(String roleCode) {
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (e.roleCode.equals(roleCode)) {
                return e.componentUrl;
            }
        }
        return null;
    }

    /**
     * * 根据 角色列表 查找 首页路由地址
     * 
     * @param roles 角色
     * @return
     */
    public static String getIndexByRoles(List<String> roles) {
        // * 初始化一个同样长度的 字符串数组
        String[] rolesArray = roles.toArray(new String[roles.size()]);

        /**
         * * 判断 当前角色 是否在 枚举 中
         * * - 如果在枚举中，则直接返回第一项
         */
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (oConvertUtils.isIn(e.roleCode, rolesArray)) {
                return e.componentUrl;
            }
        }
        // * 其他情况返回 null
        return null;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getComponentUrl() {
        return componentUrl;
    }

    public void setComponentUrl(String componentUrl) {
        this.componentUrl = componentUrl;
    }
}
