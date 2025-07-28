package org.jeecg.modules.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.mapper.SysPermissionDataRuleMapper;
import org.jeecg.modules.system.mapper.SysPermissionMapper;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * * 菜单权限规则 服务实现类
 *
 * @Author huangzhilin
 * @since 2019-04-01
 */
@Service
public class SysPermissionDataRuleImpl extends ServiceImpl<SysPermissionDataRuleMapper, SysPermissionDataRule>
		implements ISysPermissionDataRuleService {

	@Resource
	private SysPermissionMapper sysPermissionMapper;

	/**
	 * * 根据菜单id查询其对应的权限数据
	 * 
	 * @param permissionId
	 * @return List<SysPermissionDataRule>
	 */
	@Override
	public List<SysPermissionDataRule> getPermRuleListByPermId(String permissionId) {
		LambdaQueryWrapper<SysPermissionDataRule> query = new LambdaQueryWrapper<SysPermissionDataRule>();
		query.eq(SysPermissionDataRule::getPermissionId, permissionId);
		query.orderByDesc(SysPermissionDataRule::getCreateTime);
		List<SysPermissionDataRule> permRuleList = this.list(query);
		return permRuleList;
	}

	/**
	 * * 根据页面传递的参数查询菜单权限数据
	 * 
	 * @param permRule
	 * @return
	 */
	@Override
	public List<SysPermissionDataRule> queryPermissionRule(SysPermissionDataRule permRule) {
		QueryWrapper<SysPermissionDataRule> queryWrapper = QueryGenerator.initQueryWrapper(permRule, null);
		return this.list(queryWrapper);
	}

	/**
	 * * 用户在菜单下的数据权限
	 * 
	 * @param permissionId
	 * @param username
	 * @return
	 */
	@Override
	public List<SysPermissionDataRule> queryPermissionDataRules(String username, String permissionId) {
		// * 查询所有数据权限id列表
		List<String> idsList = this.baseMapper.queryDataRuleIds(username, permissionId);
		if (idsList == null || idsList.size() == 0) {
			return null;
		}

		// * 去重
		Set<String> set = new HashSet<String>();
		for (String ids : idsList) {
			if (oConvertUtils.isEmpty(ids)) {
				continue;
			}
			String[] arr = ids.split(",");
			for (String id : arr) {
				if (oConvertUtils.isNotEmpty(id) && !set.contains(id)) {
					set.add(id);
				}
			}
		}
		if (set.size() == 0) {
			return null;
		}
		// * 返回查询到的数据权限
		return this.baseMapper
				.selectList(new QueryWrapper<SysPermissionDataRule>().in("id", set).eq("status", CommonConstant.STATUS_1));
	}

	/**
	 * * 新增菜单权限配置 修改菜单rule_flag
	 * 
	 * @param sysPermissionDataRule
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void savePermissionDataRule(SysPermissionDataRule sysPermissionDataRule) {
		// * 直接保存数据权限
		this.save(sysPermissionDataRule);
		// * 找到数据权限挂在哪个菜单权限下
		SysPermission permission = sysPermissionMapper.selectById(sysPermissionDataRule.getPermissionId());
		// * 判断是否有数据权限
		boolean flag = permission != null
				&& (permission.getRuleFlag() == null || permission.getRuleFlag().equals(CommonConstant.RULE_FLAG_0));
		if (flag) {
			permission.setRuleFlag(CommonConstant.RULE_FLAG_1);
			// * 更新菜单
			sysPermissionMapper.updateById(permission);
		}
	}

	/**
	 * * 删除菜单权限配置
	 * 
	 * @param dataRuleId
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deletePermissionDataRule(String dataRuleId) {
		SysPermissionDataRule dataRule = this.baseMapper.selectById(dataRuleId);
		if (dataRule != null) {
			// * 直接删除数据权限
			this.removeById(dataRuleId);
			// * 查询菜单下是否还有数据权限
			Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<SysPermissionDataRule>()
					.eq(SysPermissionDataRule::getPermissionId, dataRule.getPermissionId()));

			// * 注:同一个事务中删除后再查询是会认为数据已被删除的 若事务回滚上述删除无效

			if (count == null || count == 0) {
				// * 如果没有的话设置配置为无
				SysPermission permission = sysPermissionMapper.selectById(dataRule.getPermissionId());
				if (permission != null && permission.getRuleFlag().equals(CommonConstant.RULE_FLAG_1)) {
					permission.setRuleFlag(CommonConstant.RULE_FLAG_0);
					sysPermissionMapper.updateById(permission);
				}
			}
		}
	}
}
