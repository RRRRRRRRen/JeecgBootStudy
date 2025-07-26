package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.vo.SysUserOnlineVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * * 在线用户
 * 
 * @Description: 在线用户
 * @Author: chenli
 * @Date: 2020-06-07
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/online")
@Slf4j
public class SysUserOnlineController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public ISysUserService userService;

    @Autowired
    private SysBaseApiImpl sysBaseApi;

    @Resource
    private BaseCommonService baseCommonService;

    /**
     * * 查询在线用户列表
     * 
     * @param username
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<Page<SysUserOnlineVO>> list(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        // * 模糊匹配所有用户token
        Collection<String> keys = redisUtil.scan(CommonConstant.PREFIX_USER_TOKEN + "*");

        // * 初始化在线用户列表
        List<SysUserOnlineVO> onlineList = new ArrayList<SysUserOnlineVO>();

        for (String key : keys) {
            // * 获取token
            String token = (String) redisUtil.get(key);
            if (StringUtils.isNotEmpty(token)) {
                // * 设置token
                SysUserOnlineVO online = new SysUserOnlineVO();
                online.setToken(token);

                // * 查询登录用户
                LoginUser loginUser = sysBaseApi.getUserByName(JwtUtil.getUsername(token));

                if (loginUser != null && !"_reserve_user_external".equals(loginUser.getUsername())) {
                    // * 验证用户名是否与传过来的用户名相同

                    boolean isMatchUsername = true;
                    // * 判断用户名是否为空，并且当前循环的用户不包含传过来的用户名，那么就设成false
                    if (oConvertUtils.isNotEmpty(username) && !loginUser.getUsername().contains(username)) {
                        isMatchUsername = false;
                    }
                    // * 查询到就加入
                    if (isMatchUsername) {
                        BeanUtils.copyProperties(loginUser, online);
                        onlineList.add(online);
                    }
                }
            }
        }

        // * 翻转
        Collections.reverse(onlineList);

        // * 初始化 分页参数
        Page<SysUserOnlineVO> page = new Page<SysUserOnlineVO>(pageNo, pageSize);
        List<SysUserOnlineVO> pages = new ArrayList<>();
        int count = onlineList.size();

        // * 手动分页
        int currId = pageNo > 1 ? (pageNo - 1) * pageSize : 0;
        for (int i = 0; i < pageSize && i < count - currId; i++) {
            pages.add(onlineList.get(currId + i));
        }
        page.setSize(pageSize);
        page.setCurrent(pageNo);
        page.setTotal(count);
        page.setPages(count % 10 == 0 ? count / 10 : count / 10 + 1);
        page.setRecords(pages);

        // * 输出
        Result<Page<SysUserOnlineVO>> result = new Result<Page<SysUserOnlineVO>>();
        result.setSuccess(true);
        result.setResult(page);
        return result;
    }

    /**
     * * 强退用户
     */
    @RequestMapping(value = "/forceLogout", method = RequestMethod.POST)
    public Result<Object> forceLogout(@RequestBody SysUserOnlineVO online) {
        // * 参数没token不退出
        if (oConvertUtils.isEmpty(online.getToken())) {
            return Result.error("退出登录失败！");
        }

        // * token中获取用户名
        String username = JwtUtil.getUsername(online.getToken());
        LoginUser sysUser = sysBaseApi.getUserByName(username);

        if (sysUser != null) {
            // * 日志
            baseCommonService.addLog("强制: " + sysUser.getRealname() + "退出成功！", CommonConstant.LOG_TYPE_1, null,
                    sysUser);
            log.info(" 强制  " + sysUser.getRealname() + "退出成功！ ");

            // * 清空用户登录Token缓存
            redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + online.getToken());

            // * 清空用户登录Shiro权限缓存
            redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + sysUser.getId());

            // * 清空用户的缓存信息（包括部门信息），例如sys:cache:user::<username>
            redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, sysUser.getUsername()));

            // * 调用shiro的logout
            SecurityUtils.getSubject().logout();
            return Result.ok("退出登录成功！");
        } else {
            return Result.error("Token无效!");
        }
    }
}
