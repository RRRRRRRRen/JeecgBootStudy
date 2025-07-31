package org.jeecg.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.system.query.QueryGenerator;

import javax.servlet.http.HttpServletRequest;

/**
 * * 规则值自动生成工具类
 */
@Slf4j
public class FillRuleUtil {

    /**
     * @param ruleCode ruleCode
     * @return
     */
    public static <M extends BaseMapper<T>, T> Object executeRule(
            String ruleCode,
            JSONObject formData,
            Class<? extends ServiceImpl<M, T>> serviceClass) {
        if (!StringUtils.isEmpty(ruleCode)) {
            try {
                // * 获取 Service 查询
                ServiceImpl<M, T> impl = SpringContextUtils.getBean(serviceClass);
                QueryWrapper<T> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("rule_code", ruleCode);
                // * 判断该规则是否已存在
                JSONObject entity = JSON.parseObject(JSON.toJSONString(impl.getOne(queryWrapper)));
                if (entity == null) {
                    log.warn("填值规则：" + ruleCode + " 不存在");
                    return null;
                }

                // * 获取必要的参数
                String ruleClass = entity.getString("ruleClass");
                JSONObject params = entity.getJSONObject("ruleParams");
                if (params == null) {
                    params = new JSONObject();
                }

                HttpServletRequest request = SpringContextUtils.getHttpServletRequest();

                // * 解析 params 中的变量
                for (String key : params.keySet()) {
                    // * 1. 判断 queryString 中是否有该参数，如果有就优先取值
                    if (request != null) {
                        String parameter = request.getParameter(key);
                        if (oConvertUtils.isNotEmpty(parameter)) {
                            params.put(key, parameter);
                            continue;
                        }
                    }
                    String value = params.getString(key);

                    // * 2. 用于替换 系统变量的值 #{sys_user_code}
                    if (value != null && value.contains(SymbolConstant.SYS_VAR_PREFIX)) {
                        value = QueryGenerator.getSqlRuleValue(value);
                        params.put(key, value);
                    }
                }

                if (formData == null) {
                    formData = new JSONObject();
                }

                // * 通过反射执行配置的类里的方法
                IFillRuleHandler ruleHandler = (IFillRuleHandler) Class.forName(ruleClass).newInstance();
                return ruleHandler.execute(params, formData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
