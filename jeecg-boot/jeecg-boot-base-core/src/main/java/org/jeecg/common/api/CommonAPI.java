package org.jeecg.common.api;

import org.jeecg.common.system.vo.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通用api
 * 
 * @author: jeecg-boot
 */
public interface CommonAPI {

    /**
     * * 根据 username 查询用户角色信息
     * 
     * @param username
     * @return
     */
    Set<String> queryUserRoles(String username);

    /**
     * * 根据 userId 查询用户角色信息
     * 
     * @param userId
     * @return
     */
    Set<String> queryUserRolesById(String userId);

    /**
     * * 根据 userId 查询用户权限信息
     * 
     * @param userId
     * @return
     */
    Set<String> queryUserAuths(String userId);

    /**
     * TODO 未知功能的接口
     * * 根据 dbSourceId 查询数据库中存储的 DynamicDataSourceModel
     * 
     * @param dbSourceId
     * @return
     */
    DynamicDataSourceModel getDynamicDbSourceById(String dbSourceId);

    /**
     * TODO 未知功能的接口
     * * 根据 dbSourceCode 查询数据库中存储的 DynamicDataSourceModel
     *
     * @param dbSourceCode
     * @return
     */
    DynamicDataSourceModel getDynamicDbSourceByCode(String dbSourceCode);

    /**
     * * 根据 username 查询用户信息
     * 
     * @param username
     * @return
     */
    public LoginUser getUserByName(String username);

    /**
     * * 根据 username 查询用户 userId
     * 
     * @param username
     * @return
     */
    public String getUserIdByName(String username);

    /**
     * TODO 未知的参数使用方法
     * 6字典表的 翻译
     * 
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    String translateDictFromTable(String table, String text, String code, String key);

    /**
     * TODO 未知的使用方法
     * 7普通字典的翻译
     * 
     * @param code
     * @param key
     * @return
     */
    String translateDict(String code, String key);

    /**
     * TODO 未知的使用方法
     * 8查询数据权限
     * 
     * @param component   组件
     * @param username    用户名
     * @param requestPath 前段请求地址
     * @return
     */
    List<SysPermissionDataRuleModel> queryPermissionDataRule(String component, String requestPath, String username);

    /**
     * TODO 未知的使用方法
     * * 根据 username 查询用户缓存信息
     * 
     * @param username
     * @return
     */
    SysUserCacheInfo getCacheUser(String username);

    /**
     * * 根据 code 查询数据字典集合
     * 
     * @param code
     * @return
     */
    public List<DictModel> queryDictItemsByCode(String code);

    /**
     * * 根据 code 有效的数据字典集合
     * 
     * @param code
     * @return
     */
    public List<DictModel> queryEnableDictItemsByCode(String code);

    /**
     * TODO 未知的使用方法
     * 13获取表数据字典
     * 
     * @param tableFilterSql
     * @param text
     * @param code
     * @return
     */
    List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code);

    /**
     * * 根据 dictCodes keys 批量获取字典翻译
     * 
     * @param dictCodes 例如：user_status,sex
     * @param keys      例如：1,2,0
     * @return
     */
    Map<String, List<DictModel>> translateManyDict(String dictCodes, String keys);

    /**
     * TODO 未知的使用方法
     * * 解决分布式下表字典跨库无法查询问题
     * 15 字典表的 翻译，可批量
     * 
     * @param table
     * @param text
     * @param code
     * @param keys       多个用逗号分割
     * @param dataSource 数据源
     * @return
     */
    List<DictModel> translateDictFromTableByKeys(String table, String text, String code, String keys,
            String dataSource);

}
