package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.FillRuleConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.FillRuleUtil;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.util.FindsDepartsChildrenUtil;
import org.jeecg.modules.system.vo.SysDepartExportVo;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门表 服务实现类
 * <p>
 * 
 * @Author Steve
 * @Since 2019-01-22
 */
@Service
public class SysDepartServiceImpl extends ServiceImpl<SysDepartMapper, SysDepart> implements ISysDepartService {

	@Autowired
	private SysUserDepartMapper userDepartMapper;
	@Autowired
	private SysDepartRoleMapper sysDepartRoleMapper;
	@Autowired
	private SysDepartPermissionMapper departPermissionMapper;
	@Autowired
	private SysDepartRolePermissionMapper departRolePermissionMapper;
	@Autowired
	private SysDepartRoleUserMapper departRoleUserMapper;
	@Autowired
	private SysUserMapper sysUserMapper;
	@Autowired
	private SysDepartMapper departMapper;

	/**
	 * * 查询我的部门信息,并分节点进行显示
	 * 
	 * @param departIds 部门id
	 * @return
	 */
	@Override
	public List<SysDepartTreeModel> queryMyDeptTreeList(String departIds) {
		// * 根据部门id获取所负责部门
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		// * 根据部门ids 获取 父级部门编码
		String[] codeArr = this.getMyDeptParentOrgCode(departIds);
		if (ArrayUtil.isEmpty(codeArr)) {
			return null;
		}

		// * 查询条件
		for (int i = 0; i < codeArr.length; i++) {
			query.or().likeRight(SysDepart::getOrgCode, codeArr[i]);
		}
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		query.orderByAsc(SysDepart::getDepartOrder);

		// * 将父节点ParentId设为null
		List<SysDepart> listDepts = this.list(query);
		for (int i = 0; i < codeArr.length; i++) {
			for (SysDepart dept : listDepts) {
				if (dept.getOrgCode().equals(codeArr[i])) {
					dept.setParentId(null);
				}
			}
		}

		// * 生成树
		List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(listDepts);
		return listResult;
	}

	/**
	 * * 查询所有部门信息,并分节点进行显示
	 * 
	 * @return
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeList() {
		// * 查询
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);

		// * 设置部门负责人
		this.setUserIdsByDepList(list);
		// * 生成树状数据
		List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(list);
		return listResult;
	}

	/**
	 * * 根据部门id查询所有部门信息,平铺显示
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeList(String ids) {
		List<SysDepartTreeModel> listResult = new ArrayList<>();
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		if (oConvertUtils.isNotEmpty(ids)) {
			query.in(true, SysDepart::getId, Arrays.asList(ids.split(",")));
		}
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);

		for (SysDepart depart : list) {
			listResult.add(new SysDepartTreeModel(depart));
		}
		return listResult;

	}

	// @Cacheable(value = CacheConstant.SYS_DEPART_IDS_CACHE)
	@Override
	public List<DepartIdModel> queryDepartIdTreeList() {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		// ------------------------------------------------------------------------------------------------
		// 是否开启系统管理模块的多租户数据隔离【SAAS多租户模式】
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		// ------------------------------------------------------------------------------------------------
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);
		// 调用wrapTreeDataToTreeList方法生成树状数据
		List<DepartIdModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToDepartIdTreeList(list);
		return listResult;
	}

	/**
	 * * 保存部门数据
	 * 
	 * @param sysDepart
	 * @param username  用户名
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveDepartData(SysDepart sysDepart, String username) {
		if (sysDepart != null && username != null) {
			if (oConvertUtils.isEmpty(sysDepart.getParentId())) {
				// * 不存在父节点 则置空
				sysDepart.setParentId("");
			} else {
				// * 存在父节点 将父部门的设成不是叶子结点
				departMapper.setMainLeaf(sysDepart.getParentId(), CommonConstant.NOT_LEAF);
			}

			// * 设置id
			sysDepart.setId(IdWorker.getIdStr(sysDepart));

			// * 获取父级ID
			String parentId = sysDepart.getParentId();
			JSONObject formData = new JSONObject();
			formData.put("parentId", parentId);

			// * 部门编码规则生成
			String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART, formData,
					SysFillRuleServiceImpl.class);
			sysDepart.setOrgCode(codeArray[0]);
			String orgType = codeArray[1];
			sysDepart.setOrgType(String.valueOf(orgType));
			sysDepart.setCreateTime(new Date());
			sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
			// * 新添加的部门是叶子节点
			sysDepart.setIzLeaf(CommonConstant.IS_LEAF);

			// * 数据库默认值兼容
			if (oConvertUtils.isEmpty(sysDepart.getOrgCategory())) {
				if (oConvertUtils.isEmpty(sysDepart.getParentId())) {
					sysDepart.setOrgCategory("1");
				} else {
					sysDepart.setOrgCategory("2");
				}
			}

			// * 保存
			this.save(sysDepart);

			// * 部门负责人处理
			if (oConvertUtils.isNotEmpty(sysDepart.getDirectorUserIds())) {
				this.addDepartByUserIds(sysDepart, sysDepart.getDirectorUserIds());
			}
		}
	}

	/**
	 * saveDepartData 的调用方法,生成部门编码和部门类型（作废逻辑）
	 * 
	 * @deprecated
	 * @param parentId
	 * @return
	 */
	private String[] generateOrgCode(String parentId) {
		// update-begin--Author:Steve Date:20190201 for：组织机构添加数据代码调整
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		LambdaQueryWrapper<SysDepart> query1 = new LambdaQueryWrapper<SysDepart>();
		String[] strArray = new String[2];
		// 创建一个List集合,存储查询返回的所有SysDepart对象
		List<SysDepart> departList = new ArrayList<>();
		// 定义新编码字符串
		String newOrgCode = "";
		// 定义旧编码字符串
		String oldOrgCode = "";
		// 定义部门类型
		String orgType = "";
		// 如果是最高级,则查询出同级的org_code, 调用工具类生成编码并返回
		if (StringUtil.isNullOrEmpty(parentId)) {
			// 线判断数据库中的表是否为空,空则直接返回初始编码
			query1.eq(SysDepart::getParentId, "").or().isNull(SysDepart::getParentId);
			query1.orderByDesc(SysDepart::getOrgCode);
			departList = this.list(query1);
			if (departList == null || departList.size() == 0) {
				strArray[0] = YouBianCodeUtil.getNextYouBianCode(null);
				strArray[1] = "1";
				return strArray;
			} else {
				SysDepart depart = departList.get(0);
				oldOrgCode = depart.getOrgCode();
				orgType = depart.getOrgType();
				newOrgCode = YouBianCodeUtil.getNextYouBianCode(oldOrgCode);
			}
		} else { // 反之则查询出所有同级的部门,获取结果后有两种情况,有同级和没有同级
			// 封装查询同级的条件
			query.eq(SysDepart::getParentId, parentId);
			// 降序排序
			query.orderByDesc(SysDepart::getOrgCode);
			// 查询出同级部门的集合
			List<SysDepart> parentList = this.list(query);
			// 查询出父级部门
			SysDepart depart = this.getById(parentId);
			// 获取父级部门的Code
			String parentCode = depart.getOrgCode();
			// 根据父级部门类型算出当前部门的类型
			orgType = String.valueOf(Integer.valueOf(depart.getOrgType()) + 1);
			// 处理同级部门为null的情况
			if (parentList == null || parentList.size() == 0) {
				// 直接生成当前的部门编码并返回
				newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, null);
			} else { // 处理有同级部门的情况
				// 获取同级部门的编码,利用工具类
				String subCode = parentList.get(0).getOrgCode();
				// 返回生成的当前部门编码
				newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, subCode);
			}
		}
		// 返回最终封装了部门编码和部门类型的数组
		strArray[0] = newOrgCode;
		strArray[1] = orgType;
		return strArray;
		// update-end--Author:Steve Date:20190201 for：组织机构添加数据代码调整
	}

	/**
	 * * 更新depart数据
	 * 
	 * @param sysDepart
	 * @param username  用户名
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateDepartDataById(SysDepart sysDepart, String username) {
		if (sysDepart != null && username != null) {
			sysDepart.setUpdateTime(new Date());
			sysDepart.setUpdateBy(username);
			this.updateById(sysDepart);
			// * 修改部门管理的时候，修改负责部门
			this.updateChargeDepart(sysDepart);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * TODO 根据部门id批量删除并删除其可能存在的子级部门
	 * 
	 * @param ids 多个部门id
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteBatchWithChildren(List<String> ids) {
		// 存放子级的id
		List<String> idList = new ArrayList<String>();
		// 存放父级的id
		List<String> parentIdList = new ArrayList<>();
		for (String id : ids) {
			idList.add(id);
			// 此步骤是为了删除子级
			this.checkChildrenExists(id, idList);
			// update-begin---author:wangshuai ---date:20230712
			// for：【QQYUN-5757】批量删除部门时未正确置为叶子节点 ------------
			SysDepart depart = this.getDepartById(id);
			if (oConvertUtils.isNotEmpty(depart.getParentId())) {
				if (!parentIdList.contains(depart.getParentId())) {
					parentIdList.add(depart.getParentId());
				}
			}
			// update-end---author:wangshuai ---date:20230712
			// for：【QQYUN-5757】批量删除部门时未正确置为叶子节点 ------------
		}
		this.removeByIds(idList);
		// update-begin---author:wangshuai ---date:20230712
		// for：【QQYUN-5757】批量删除部门时未正确置为叶子节点 ------------
		// 再删除前需要获取父级id，不然会一直为空
		this.setParentDepartIzLeaf(parentIdList);
		// update-end---author:wangshuai ---date:20230712
		// for：【QQYUN-5757】批量删除部门时未正确置为叶子节点 ------------
		// 根据部门id获取部门角色id
		List<String> roleIdList = new ArrayList<>();
		LambdaQueryWrapper<SysDepartRole> query = new LambdaQueryWrapper<>();
		query.select(SysDepartRole::getId).in(SysDepartRole::getDepartId, idList);
		List<SysDepartRole> depRoleList = sysDepartRoleMapper.selectList(query);
		for (SysDepartRole deptRole : depRoleList) {
			roleIdList.add(deptRole.getId());
		}
		// 根据部门id删除用户与部门关系
		userDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getDepId, idList));
		// 根据部门id删除部门授权
		departPermissionMapper
				.delete(new LambdaQueryWrapper<SysDepartPermission>().in(SysDepartPermission::getDepartId, idList));
		// 根据部门id删除部门角色
		sysDepartRoleMapper.delete(new LambdaQueryWrapper<SysDepartRole>().in(SysDepartRole::getDepartId, idList));
		if (roleIdList != null && roleIdList.size() > 0) {
			// 根据角色id删除部门角色授权
			departRolePermissionMapper
					.delete(new LambdaQueryWrapper<SysDepartRolePermission>().in(SysDepartRolePermission::getRoleId, roleIdList));
			// 根据角色id删除部门角色用户信息
			departRoleUserMapper
					.delete(new LambdaQueryWrapper<SysDepartRoleUser>().in(SysDepartRoleUser::getDroleId, roleIdList));
		}
	}

	/**
	 * * 根据部门Id查询,当前和下级所有部门IDS
	 * 
	 * @param departId
	 * @return
	 */
	@Override
	public List<String> getSubDepIdsByDepId(String departId) {
		return this.baseMapper.getSubDepIdsByDepId(departId);
	}

	/**
	 * 获取我的部门下级所有部门IDS
	 * 
	 * @param departIds 多个部门id
	 * @return
	 */
	@Override
	public List<String> getMySubDepIdsByDepId(String departIds) {
		// 根据部门id获取所负责部门
		String[] codeArr = this.getMyDeptParentOrgCode(departIds);
		if (codeArr == null || codeArr.length == 0) {
			return null;
		}
		return this.baseMapper.getSubDepIdsByOrgCodes(codeArr);
	}

	/**
	 * <p>
	 * 根据关键字搜索相关的部门数据
	 * </p>
	 */
	@Override
	public List<SysDepartTreeModel> searchByKeyWord(String keyWord, String myDeptSearch, String departIds) {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		List<SysDepartTreeModel> newList = new ArrayList<>();
		// myDeptSearch不为空时为我的部门搜索，只搜索所负责部门
		if (!StringUtil.isNullOrEmpty(myDeptSearch)) {
			// departIds 为空普通用户或没有管理部门
			if (StringUtil.isNullOrEmpty(departIds)) {
				return newList;
			}
			// 根据部门id获取所负责部门
			String[] codeArr = this.getMyDeptParentOrgCode(departIds);
			// update-begin-author:taoyan date:20220104 for:/issues/3311
			// 当用户属于两个部门的时候，且这两个部门没有上下级关系，我的部门-部门名称查询条件模糊搜索失效！
			if (codeArr != null && codeArr.length > 0) {
				query.nested(i -> {
					for (String s : codeArr) {
						i.or().likeRight(SysDepart::getOrgCode, s);
					}
				});
			}
			// update-end-author:taoyan date:20220104 for:/issues/3311
			// 当用户属于两个部门的时候，且这两个部门没有上下级关系，我的部门-部门名称查询条件模糊搜索失效！
			query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		}
		query.like(SysDepart::getDepartName, keyWord);
		// update-begin--Author:huangzhilin Date:20140417
		// for：[bugfree号]组织机构搜索回显优化--------------------
		SysDepartTreeModel model = new SysDepartTreeModel();
		List<SysDepart> departList = this.list(query);
		if (departList.size() > 0) {
			for (SysDepart depart : departList) {
				model = new SysDepartTreeModel(depart);
				model.setChildren(null);
				// update-end--Author:huangzhilin Date:20140417
				// for：[bugfree号]组织机构搜索功回显优化----------------------
				newList.add(model);
			}
			return newList;
		}
		return null;
	}

	/**
	 * 根据部门id删除并且删除其可能存在的子级任何部门
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean delete(String id) {
		List<String> idList = new ArrayList<>();
		idList.add(id);
		this.checkChildrenExists(id, idList);
		// 清空部门树内存
		// FindsDepartsChildrenUtil.clearDepartIdModel();
		boolean ok = this.removeByIds(idList);
		// 根据部门id获取部门角色id
		List<String> roleIdList = new ArrayList<>();
		LambdaQueryWrapper<SysDepartRole> query = new LambdaQueryWrapper<>();
		query.select(SysDepartRole::getId).in(SysDepartRole::getDepartId, idList);
		List<SysDepartRole> depRoleList = sysDepartRoleMapper.selectList(query);
		for (SysDepartRole deptRole : depRoleList) {
			roleIdList.add(deptRole.getId());
		}
		// 根据部门id删除用户与部门关系
		userDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getDepId, idList));
		// 根据部门id删除部门授权
		departPermissionMapper
				.delete(new LambdaQueryWrapper<SysDepartPermission>().in(SysDepartPermission::getDepartId, idList));
		// 根据部门id删除部门角色
		sysDepartRoleMapper.delete(new LambdaQueryWrapper<SysDepartRole>().in(SysDepartRole::getDepartId, idList));
		if (roleIdList != null && roleIdList.size() > 0) {
			// 根据角色id删除部门角色授权
			departRolePermissionMapper
					.delete(new LambdaQueryWrapper<SysDepartRolePermission>().in(SysDepartRolePermission::getRoleId, roleIdList));
			// 根据角色id删除部门角色用户信息
			departRoleUserMapper
					.delete(new LambdaQueryWrapper<SysDepartRoleUser>().in(SysDepartRoleUser::getDroleId, roleIdList));
		}
		return ok;
	}

	/**
	 * delete 方法调用
	 * 
	 * @param id
	 * @param idList
	 */
	private void checkChildrenExists(String id, List<String> idList) {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getParentId, id);
		List<SysDepart> departList = this.list(query);
		if (departList != null && departList.size() > 0) {
			for (SysDepart depart : departList) {
				idList.add(depart.getId());
				this.checkChildrenExists(depart.getId(), idList);
			}
		}
	}

	/**
	 * * 根据 userId 查询SysDepart集合
	 * 
	 * @param userId
	 * @return
	 */
	@Override
	public List<SysDepart> queryUserDeparts(String userId) {
		return baseMapper.queryUserDeparts(userId);
	}

	@Override
	public List<SysDepart> queryDepartsByUsername(String username) {
		return baseMapper.queryDepartsByUsername(username);
	}

	@Override
	public List<String> queryDepartsByUserId(String userId) {
		List<String> list = baseMapper.queryDepartsByUserId(userId);
		return list;
	}

	/**
	 * * 根据部门ids 获取 父级部门编码
	 * 
	 * @param departIds
	 * @return
	 */
	private String[] getMyDeptParentOrgCode(String departIds) {
		// * 根据部门id查询所负责部门
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		if (oConvertUtils.isNotEmpty(departIds)) {
			query.in(SysDepart::getId, Arrays.asList(departIds.split(",")));
		}

		// * 是否开启系统管理模块的多租户数据隔离【SAAS多租户模式】
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}

		// * 根据编码排序
		query.orderByAsc(SysDepart::getOrgCode);

		// * 返回查询到的部门列表
		List<SysDepart> list = this.list(query);
		if (list == null || list.size() == 0) {
			return null;
		}

		// * 查找根部门
		String orgCode = this.getMyDeptParentNode(list);
		String[] codeArr = orgCode.split(",");
		return codeArr;
	}

	/**
	 * * 获取负责部门父节点
	 * 
	 * @param list
	 * @return
	 */
	private String getMyDeptParentNode(List<SysDepart> list) {
		Map<String, String> map = new HashMap<>(5);
		// * 1.先将同一公司归类
		for (SysDepart dept : list) {
			// * 提取一级分类
			String code = dept.getOrgCode().substring(0, 3);

			// * 更具一级分类 添加 orgCode
			if (map.containsKey(code)) {
				String mapCode = map.get(code) + "," + dept.getOrgCode();
				map.put(code, mapCode);
			} else {
				map.put(code, dept.getOrgCode());
			}
		}

		// * 2.获取同一公司的根节点
		StringBuffer parentOrgCode = new StringBuffer();
		for (String str : map.values()) {
			// * 获取 orgCode 数组
			String[] arrStr = str.split(",");
			parentOrgCode.append(",").append(this.getMinLengthNode(arrStr));
		}
		return parentOrgCode.substring(1);
	}

	/**
	 * * 获取同一公司中部门编码长度最小的部门
	 * 
	 * @param str
	 * @return
	 */
	private String getMinLengthNode(String[] str) {
		int min = str[0].length();
		StringBuilder orgCodeBuilder = new StringBuilder(str[0]);
		for (int i = 1; i < str.length; i++) {
			if (str[i].length() <= min) {
				min = str[i].length();
				orgCodeBuilder.append(SymbolConstant.COMMA).append(str[i]);
			}
		}
		return orgCodeBuilder.toString();
	}

	/**
	 * 获取部门树信息根据关键字
	 * 
	 * @param keyWord
	 * @return
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeByKeyWord(String keyWord) {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);
		// 调用wrapTreeDataToTreeList方法生成树状数据
		List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(list);
		List<SysDepartTreeModel> treelist = new ArrayList<>();
		if (StringUtils.isNotBlank(keyWord)) {
			this.getTreeByKeyWord(keyWord, listResult, treelist);
		} else {
			return listResult;
		}
		return treelist;
	}

	/**
	 * * 根据parentId查询部门树
	 * 
	 * * - 看起来应该是平铺的数据
	 * 
	 * @param parentId
	 * @param ids        前端回显传递
	 * @param primaryKey 主键字段（id或者orgCode）
	 * @return
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeListByPid(String parentId, String ids, String primaryKey) {
		Consumer<LambdaQueryWrapper<SysDepart>> square = i -> {
			if (oConvertUtils.isNotEmpty(ids)) {
				if (CommonConstant.DEPART_KEY_ORG_CODE.equals(primaryKey)) {
					// * 按 org_code 查询
					i.in(SysDepart::getOrgCode, Arrays.asList(ids.split(SymbolConstant.COMMA)));
				} else {
					// * 按 id 查询
					i.in(SysDepart::getId, Arrays.asList(ids.split(SymbolConstant.COMMA)));
				}
			} else {
				if (oConvertUtils.isEmpty(parentId)) {
					// * 查询 parentId 为 null 或 "" 的节点（顶级节点）
					i.and(q -> q.isNull(true, SysDepart::getParentId).or().eq(true, SysDepart::getParentId, ""));
				} else {
					// * 查询指定父节点下的子节点
					i.eq(true, SysDepart::getParentId, parentId);
				}
			}
		};

		// * 执行查询
		LambdaQueryWrapper<SysDepart> lqw = new LambdaQueryWrapper<>();
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			lqw.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		lqw.eq(true, SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		lqw.func(square);
		lqw.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = list(lqw);

		// * 设置部门负责人
		this.setUserIdsByDepList(list);

		// * 转化为 SysDepartTreeModel
		List<SysDepartTreeModel> records = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			SysDepart depart = list.get(i);
			SysDepartTreeModel treeModel = new SysDepartTreeModel(depart);
			records.add(treeModel);
		}
		return records;
	}

	/**
	 * * 获取某个部门的所有父级部门的ID
	 *
	 * @param departId 根据departId查
	 * @return JSONObject
	 */
	@Override
	public JSONObject queryAllParentIdByDepartId(String departId) {
		JSONObject result = new JSONObject();
		for (String id : departId.split(SymbolConstant.COMMA)) {
			JSONObject all = this.queryAllParentId("id", id);
			result.put(id, all);
		}
		return result;
	}

	/**
	 * * 获取某个部门的所有父级部门的ID
	 *
	 * @param orgCode 根据orgCode查
	 * @return JSONObject
	 */
	@Override
	public JSONObject queryAllParentIdByOrgCode(String orgCode) {
		JSONObject result = new JSONObject();
		for (String code : orgCode.split(SymbolConstant.COMMA)) {
			JSONObject all = this.queryAllParentId("org_code", code);
			result.put(code, all);
		}
		return result;
	}

	/**
	 * * 查询某个部门的所有父ID信息
	 *
	 * @param fieldName 字段名
	 * @param value     值
	 */
	private JSONObject queryAllParentId(String fieldName, String value) {
		JSONObject data = new JSONObject();
		// * 父ID集合，有序
		data.put("parentIds", new JSONArray());
		// * 父ID的部门数据，key是id，value是数据
		data.put("parentMap", new JSONObject());
		this.queryAllParentIdRecursion(fieldName, value, data);
		return data;
	}

	/**
	 * * 递归调用查询父部门接口
	 */
	private void queryAllParentIdRecursion(String fieldName, String value, JSONObject data) {
		// * 查询指定部门
		QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(fieldName, value);
		SysDepart depart = super.getOne(queryWrapper);

		// * 将当前部门数据填充到data中
		if (depart != null) {
			data.getJSONArray("parentIds").add(0, depart.getId());
			data.getJSONObject("parentMap").put(depart.getId(), depart);
			if (oConvertUtils.isNotEmpty(depart.getParentId())) {
				// * 递归填充父级
				this.queryAllParentIdRecursion("id", depart.getParentId(), data);
			}
		}
	}

	@Override
	public SysDepart queryCompByOrgCode(String orgCode) {
		int length = YouBianCodeUtil.ZHANWEI_LENGTH;
		String compyOrgCode = orgCode.substring(0, length);
		return this.baseMapper.queryCompByOrgCode(compyOrgCode);
	}

	/**
	 * 根据id查询下级部门
	 * 
	 * @param pid
	 * @return
	 */
	@Override
	public List<SysDepart> queryDeptByPid(String pid) {
		return this.baseMapper.queryDeptByPid(pid);
	}

	/**
	 * 根据关键字筛选部门信息
	 * 
	 * @param keyWord
	 * @return
	 */
	public void getTreeByKeyWord(String keyWord, List<SysDepartTreeModel> allResult, List<SysDepartTreeModel> newResult) {
		for (SysDepartTreeModel model : allResult) {
			if (model.getDepartName().contains(keyWord)) {
				newResult.add(model);
				continue;
			} else if (model.getChildren() != null) {
				getTreeByKeyWord(keyWord, model.getChildren(), newResult);
			}
		}
	}

	/**
	 * * 通过用户id设置负责部门
	 * 
	 * @param sysDepart SysDepart部门对象
	 * @param userIds   多个负责用户id
	 */
	public void addDepartByUserIds(SysDepart sysDepart, String userIds) {
		// * 获取部门id,保存到用户
		String departId = sysDepart.getId();
		// * 循环用户id
		String[] userIdArray = userIds.split(",");
		for (String userId : userIdArray) {
			// * 查询用户表增加负责部门
			SysUser sysUser = sysUserMapper.selectById(userId);
			// * 如果部门id不为空，那么就需要拼接
			if (oConvertUtils.isNotEmpty(sysUser.getDepartIds())) {
				if (!sysUser.getDepartIds().contains(departId)) {
					sysUser.setDepartIds(sysUser.getDepartIds() + "," + departId);
				}
			} else {
				sysUser.setDepartIds(departId);
			}
			// * 设置身份为上级
			sysUser.setUserIdentity(CommonConstant.USER_IDENTITY_2);

			// * 跟新用户表
			sysUserMapper.updateById(sysUser);
			// * 判断当前用户是否包含所属部门
			List<SysUserDepart> userDepartList = userDepartMapper.getUserDepartByUid(userId);
			boolean isExistDepId = userDepartList.stream().anyMatch(item -> departId.equals(item.getDepId()));
			// * 如果不存在需要设置所属部门
			if (!isExistDepId) {
				userDepartMapper.insert(new SysUserDepart(userId, departId));
			}
		}
	}

	/**
	 * * 修改用户负责部门
	 * 
	 * @param sysDepart SysDepart对象
	 */
	private void updateChargeDepart(SysDepart sysDepart) {
		// * 新的用户id
		String directorIds = sysDepart.getDirectorUserIds();
		// * 旧的用户id（数据库中存在的）
		String oldDirectorIds = sysDepart.getOldDirectorUserIds();
		// * 部门id
		String departId = sysDepart.getId();

		if (oConvertUtils.isEmpty(directorIds)) {
			// * 如果用户id为空 => 那么用户的负责部门id应该去除
			this.deleteChargeDepId(departId, null);
		} else if (oConvertUtils.isNotEmpty(directorIds) && oConvertUtils.isEmpty(oldDirectorIds)) {
			// * 如果用户id不为空但是用户原来负责部门的用户id为空 => 直接添加
			this.addDepartByUserIds(sysDepart, directorIds);
		} else {
			// * 都不为空 => 需要比较，进行添加或删除
			List<String> userIdList = Arrays.stream(oldDirectorIds.split(",")).filter(item -> !directorIds.contains(item))
					.collect(Collectors.toList());
			for (String userId : userIdList) {
				this.deleteChargeDepId(departId, userId);
			}
			String addUserIds = Arrays.stream(directorIds.split(",")).filter(item -> !oldDirectorIds.contains(item))
					.collect(Collectors.joining(","));
			if (oConvertUtils.isNotEmpty(addUserIds)) {
				this.addDepartByUserIds(sysDepart, addUserIds);
			}
		}
	}

	/**
	 * * 删除用户负责部门
	 * 
	 * @param departId 部门id
	 * @param userId   用户id
	 */
	private void deleteChargeDepId(String departId, String userId) {
		// * 先查询负责部门的用户id,因为负责部门的id使用逗号拼接起来的
		LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
		query.like(SysUser::getDepartIds, departId);
		// * 删除全部的情况下用户id不存在
		if (oConvertUtils.isNotEmpty(userId)) {
			query.eq(SysUser::getId, userId);
		}
		List<SysUser> userList = sysUserMapper.selectList(query);
		for (SysUser sysUser : userList) {
			// * 将不存在的部门id删除掉
			String departIds = sysUser.getDepartIds();
			List<String> list = new ArrayList<>(Arrays.asList(departIds.split(",")));
			list.remove(departId);
			// * 删除之后再将新的id用逗号拼接起来进行更新
			String newDepartIds = String.join(",", list);
			sysUser.setDepartIds(newDepartIds);
			sysUserMapper.updateById(sysUser);
		}
	}

	/**
	 * * 通过部门集合为部门设置用户id，用于前台展示
	 * 
	 * @param departList SysDepart 部门集合
	 */
	private void setUserIdsByDepList(List<SysDepart> departList) {
		// * 查询负责部门不为空的用户
		LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
		query.isNotNull(SysUser::getDepartIds);
		List<SysUser> users = sysUserMapper.selectList(query);

		// * 根据 departId 分类 负责人 userid
		Map<String, Object> map = new HashMap<>(5);
		for (SysUser user : users) {
			String departIds = user.getDepartIds();
			String[] departIdArray = departIds.split(",");
			for (String departId : departIdArray) {
				if (map.containsKey(departId)) {
					String userIds = map.get(departId) + "," + user.getId();
					map.put(departId, userIds);
				} else {
					map.put(departId, user.getId());
				}
			}
		}
		// * 循环部门集合找到部门id对应的负责用户
		for (SysDepart sysDepart : departList) {
			if (map.containsKey(sysDepart.getId())) {
				sysDepart.setDirectorUserIds(map.get(sysDepart.getId()).toString());
			}
		}
	}

	/**
	 * 获取我的部门已加入的公司
	 * 
	 * @return
	 */
	@Override
	public List<SysDepart> getMyDepartList() {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String userId = user.getId();
		// 字典code集合
		List<String> list = new ArrayList<>();
		// 查询我加入的部门
		List<SysDepart> sysDepartList = this.baseMapper.queryUserDeparts(userId);
		for (SysDepart sysDepart : sysDepartList) {
			// 获取一级部门编码
			String orgCode = sysDepart.getOrgCode();
			if (YouBianCodeUtil.ZHANWEI_LENGTH <= orgCode.length()) {
				int length = YouBianCodeUtil.ZHANWEI_LENGTH;
				String companyOrgCode = orgCode.substring(0, length);
				list.add(companyOrgCode);
			}
		}
		// 字典code集合不为空
		if (oConvertUtils.isNotEmpty(list)) {
			// 查询一级部门的数据
			LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
			query.select(SysDepart::getDepartName, SysDepart::getId, SysDepart::getOrgCode);
			query.eq(SysDepart::getDelFlag, String.valueOf(CommonConstant.DEL_FLAG_0));
			query.in(SysDepart::getOrgCode, list);
			return this.baseMapper.selectList(query);
		}
		return null;
	}

	/**
	 * * 删除部门
	 * 
	 * @param id
	 */
	@Override
	public void deleteDepart(String id) {
		// * 删除部门设置父级的叶子结点
		this.setIzLeaf(id);
		// * 删除部门
		this.delete(id);
		// * 删除部门用户关系表
		LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<SysUserDepart>()
				.eq(SysUserDepart::getDepId, id);
		this.userDepartMapper.delete(query);
	}

	@Override
	public List<SysDepartTreeModel> queryBookDepTreeSync(String parentId, Integer tenantId, String departName) {
		List<SysDepart> list = departMapper.queryBookDepTreeSync(parentId, tenantId, departName);
		List<SysDepartTreeModel> records = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			SysDepart depart = list.get(i);
			SysDepartTreeModel treeModel = new SysDepartTreeModel(depart);
			records.add(treeModel);
		}
		return records;
	}

	@Override
	public SysDepart getDepartById(String id) {
		return departMapper.getDepartById(id);
	}

	/**
	 * * 根据 parentId 查询部门信息
	 * 
	 * @param parentId
	 * @return
	 */
	@Override
	public IPage<SysDepart> getMaxCodeDepart(Page<SysDepart> page, String parentId) {
		return page.setRecords(departMapper.getMaxCodeDepart(page, parentId));
	}

	/**
	 * * 更新叶子状态
	 */
	@Override
	public void updateIzLeaf(String id, Integer izLeaf) {
		departMapper.setMainLeaf(id, izLeaf);
	}

	/**
	 * * 设置父级节点是否存在叶子结点
	 * 
	 * @param id
	 */
	private void setIzLeaf(String id) {
		SysDepart depart = this.getDepartById(id);
		String parentId = depart.getParentId();
		if (oConvertUtils.isNotEmpty(parentId)) {
			Long count = this.count(new QueryWrapper<SysDepart>().lambda().eq(SysDepart::getParentId, parentId));
			if (count == 1) {
				// * 若父节点无其他子节点，则该父节点是叶子节点
				departMapper.setMainLeaf(parentId, CommonConstant.IS_LEAF);
			}
		}
	}

	// ========================begin 零代码下部门与人员导出
	// ==================================================================

	@Override
	public List<ExportDepartVo> getExcelDepart(int tenantId) {
		// 获取父级部门
		List<ExportDepartVo> parentDepart = departMapper.getDepartList("", tenantId);
		// 子部门
		List<ExportDepartVo> childrenDepart = new ArrayList<>();
		// 把一级部门名称放在里面
		List<ExportDepartVo> exportDepartVoList = new ArrayList<>();
		// 存放部门一级id避免重复
		List<String> departIdList = new ArrayList<>();
		for (ExportDepartVo departVo : parentDepart) {
			departIdList.add(departVo.getId());
			departVo.setDepartNameUrl(departVo.getDepartName());
			exportDepartVoList.add(departVo);
			// 创建路径
			List<String> path = new ArrayList<>();
			path.add(departVo.getDepartName());
			// 创建子部门路径
			findPath(departVo, path, tenantId, childrenDepart, departIdList);
			path.clear();
		}
		exportDepartVoList.addAll(childrenDepart);
		childrenDepart.clear();
		departIdList.clear();
		return exportDepartVoList;
	}

	/**
	 * 寻找部门路径
	 * 
	 * @param departVo       部门vo
	 * @param path           部门路径
	 * @param tenantId       租户id
	 * @param childrenDepart 子部门
	 * @param departIdList   部门id集合
	 */
	private void findPath(ExportDepartVo departVo, List<String> path, Integer tenantId,
			List<ExportDepartVo> childrenDepart, List<String> departIdList) {
		// 获取租户id和部门父id获取的部门数据
		List<ExportDepartVo> departList = departMapper.getDepartList(departVo.getId(), tenantId);
		// 部门为空判断
		if (departList == null || departList.size() <= 0) {
			if (!departIdList.contains(departVo.getId())) {
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH, path));
				childrenDepart.add(departVo);
			}
			return;
		}

		for (int i = 0; i < departList.size(); i++) {
			ExportDepartVo exportDepartVo = departList.get(i);
			// 存放子级路径
			List<String> cPath = new ArrayList<>();
			cPath.addAll(path);
			cPath.add(exportDepartVo.getDepartName());
			if (!departIdList.contains(departVo.getId())) {
				departIdList.add(departVo.getId());
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH, path));
				childrenDepart.add(departVo);
			}
			findPath(exportDepartVo, cPath, tenantId, childrenDepart, departIdList);
		}
	}
	// ========================end 零代码下部门与人员导出
	// ==================================================================

	// ========================begin 零代码下部门与人员导入
	// ==================================================================
	@Override
	public void importExcel(List<ExportDepartVo> listSysDeparts, List<String> errorMessageList) {
		int num = 0;
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);

		// 部门路径排序
		Collections.sort(listSysDeparts, new Comparator<ExportDepartVo>() {
			@Override
			public int compare(ExportDepartVo o1, ExportDepartVo o2) {
				if (oConvertUtils.isNotEmpty(o1.getDepartNameUrl()) && oConvertUtils.isNotEmpty(o2.getDepartNameUrl())) {
					int oldLength = o1.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					int newLength = o2.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					return oldLength - newLength;
				} else {
					return 0;
				}
			}
		});
		// 存放部门数据的map
		Map<String, SysDepart> departMap = new HashMap<>();
		// 循环第二遍导入数据
		for (ExportDepartVo exportDepartVo : listSysDeparts) {
			SysDepart sysDepart = new SysDepart();
			// orgCode编码长度
			int codeLength = YouBianCodeUtil.ZHANWEI_LENGTH;
			Boolean izExport = false;
			try {
				izExport = this.addDepartByName(exportDepartVo.getDepartNameUrl(), exportDepartVo.getDepartName(), sysDepart,
						errorMessageList, tenantId, departMap, num);
			} catch (Exception e) {
				// 没有查找到parentDept
			}
			// 没有错误的时候才会导入数据
			if (izExport) {
				sysDepart.setOrgType(sysDepart.getOrgCode().length() / codeLength + "");
				sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
				sysDepart.setOrgCategory("1");
				sysDepart.setTenantId(tenantId);
				ImportExcelUtil.importDateSaveOne(sysDepart, ISysDepartService.class, errorMessageList, num,
						CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE);
				departMap.put(exportDepartVo.getDepartNameUrl(), sysDepart);
			}
			num++;
		}
	}

	/**
	 * 添加部门
	 * 
	 * @param departNameUrl    部门路径
	 * @param departName       部门名称
	 * @param sysDepart        部门类
	 * @param errorMessageList 错误集合
	 * @param tenantId         租户id
	 * @param departMap        部门数组。避免存在部门信息再次查询 key 存放部门路径 value 存放部门对象
	 * @param num              判断第几行有错误信息
	 */
	private Boolean addDepartByName(String departNameUrl, String departName, SysDepart sysDepart,
			List<String> errorMessageList, Integer tenantId, Map<String, SysDepart> departMap, int num) {
		int lineNumber = num + 1;
		if (oConvertUtils.isEmpty(departNameUrl) && oConvertUtils.isEmpty(departName)) {
			// 部门路径为空
			errorMessageList.add("第 " + lineNumber + " 行：记录部门路径或者部门名称为空禁止导入");
			return false;
		}
		// 获取部门名称路径
		String name = "";
		if (departNameUrl.contains(SymbolConstant.SINGLE_SLASH)) {
			// 获取分割的部门名称
			name = departNameUrl.substring(departNameUrl.lastIndexOf(SymbolConstant.SINGLE_SLASH) + 1);
		} else {
			name = departNameUrl;
		}

		if (!name.equals(departName)) {
			// 部门名称已存在
			errorMessageList
					.add("第 " + lineNumber + " 行：记录部门路径:”" + departNameUrl + "“" + "和部门名称：“" + departName + "“不一致，请检查！");
			return false;
		} else {
			String parentId = "";
			// 判断是否包含“/”
			if (departNameUrl.contains(SymbolConstant.SINGLE_SLASH)) {
				// 获取最后一个斜杠之前的路径
				String departNames = departNameUrl.substring(0, departNameUrl.lastIndexOf(SymbolConstant.SINGLE_SLASH));
				// 判断是否已经包含部门路径
				if (departMap.containsKey(departNames)) {
					SysDepart depart = departMap.get(departNames);
					if (null != depart) {
						parentId = depart.getId();
					}
				} else {
					// 分割斜杠路径，查看数据库中是否存在此路径
					String[] departNameUrls = departNameUrl.split(SymbolConstant.SINGLE_SLASH);
					String departUrlName = departNameUrls[0];
					// 判断是否为最后一位
					int count = 0;
					SysDepart depart = new SysDepart();
					depart.setId("");
					String parentIdByName = this.getDepartListByName(departUrlName, tenantId, depart, departNameUrls, count,
							departNameUrls.length - 1, name, departMap);
					// 如果parentId不为空
					if (oConvertUtils.isNotEmpty(parentIdByName)) {
						parentId = parentIdByName;
					} else {
						// 部门名称已存在
						errorMessageList.add("第 " + lineNumber + " 行：记录部门名称“" + departName + "”上级不存在，请检查！");
						return false;
					}
				}
			}
			// 查询部门名称是否已存在
			SysDepart parentDept = null;
			// update-begin---author:wangshuai ---date:20230721
			// for：一个租户部门名称可能有多个------------
			List<SysDepart> sysDepartList = departMapper.getDepartByName(departName, tenantId, parentId);
			if (CollectionUtil.isNotEmpty(sysDepartList)) {
				parentDept = sysDepartList.get(0);
			}
			// update-end---author:wangshuai ---date:20230721 for：一个租户部门名称可能有多个------------
			if (null != parentDept) {
				// 部门名称已存在
				errorMessageList.add("第 " + lineNumber + " 行：记录部门名称“" + departName + "”已存在，请检查！");
				return false;
			} else {
				Page<SysDepart> page = new Page<>(1, 1);
				// 需要获取父级id，查看父级是否已经存在
				// 获取一级部门的最大orgCode
				List<SysDepart> records = departMapper.getMaxCodeDepart(page, parentId);
				String newOrgCode = "";
				if (CollectionUtil.isNotEmpty(records)) {
					newOrgCode = YouBianCodeUtil.getNextYouBianCode(records.get(0).getOrgCode());
				} else {
					// 查询父id
					if (oConvertUtils.isNotEmpty(parentId)) {
						SysDepart departById = departMapper.getDepartById(parentId);
						newOrgCode = YouBianCodeUtil.getSubYouBianCode(departById.getOrgCode(), null);
					} else {
						newOrgCode = YouBianCodeUtil.getNextYouBianCode(null);
					}
				}
				if (oConvertUtils.isNotEmpty(parentId)) {
					this.updateIzLeaf(parentId, CommonConstant.NOT_LEAF);
					sysDepart.setParentId(parentId);
				}
				sysDepart.setOrgCode(newOrgCode);
				sysDepart.setDepartName(departName);
				return true;
			}

		}
	}

	/**
	 * 获取部门名称url（下级）
	 * 
	 * @param departName     部门名称
	 * @param tenantId       租户id
	 * @param sysDepart      部门对象
	 * @param count          部门路径下标
	 * @param departNameUrls 部门路径
	 * @param departNum      部门路径的数量
	 * @param name           部门路径的数量
	 * @param departMap      存放部门的数据 key 存放部门路径 value 存放部门对象
	 */
	private String getDepartListByName(String departName, Integer tenantId, SysDepart sysDepart, String[] departNameUrls,
			int count, int departNum, String name, Map<String, SysDepart> departMap) {
		// 递归查找下一级
		// update-begin---author:wangshuai ---date:20230721
		// for：一个租户部门名称可能有多个------------
		SysDepart parentDept = null;
		List<SysDepart> departList = departMapper.getDepartByName(departName, tenantId, sysDepart.getId());
		if (CollectionUtil.isNotEmpty(departList)) {
			parentDept = departList.get(0);
		}
		// update-end---author:wangshuai ---date:20230721 for：一个租户部门名称可能有多个------------
		// 判断是否包含/
		if (oConvertUtils.isNotEmpty(name)) {
			name = name + SymbolConstant.SINGLE_SLASH + departName;
		} else {
			name = departName;
		}
		if (null != parentDept) {
			// 如果名称路径key不再在，添加一个，避免再次查询
			if (!departMap.containsKey(name)) {
				departMap.put(name, parentDept);
			}
			// 查询出来的部门名称和部门路径中的部门名称作比较，如果不存在直接返回空
			if (parentDept.getDepartName().equals(departNameUrls[count])) {
				count = count + 1;
				// 数量和部门数量相等说明已经到最后一位了，直接返回部门id
				if (count == departNum) {
					return parentDept.getId();
				} else {
					return this.getDepartListByName(departNameUrls[count], tenantId, parentDept, departNameUrls, count, departNum,
							name, departMap);
				}
			} else {
				return "";
			}
		} else {
			return "";
		}
	}
	// ========================end 零代码下部门与人员导入
	// ==================================================================

	/**
	 * 清空部门id
	 *
	 * @param parentIdList
	 */
	private void setParentDepartIzLeaf(List<String> parentIdList) {
		if (CollectionUtil.isNotEmpty(parentIdList)) {
			for (String parentId : parentIdList) {
				// 查询父级id没有子级的时候跟新为叶子节点
				LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
				query.eq(SysDepart::getParentId, parentId);
				Long count = departMapper.selectCount(query);
				// 当子级都不存在时，设置当前部门为叶子节点
				if (count == 0) {
					departMapper.setMainLeaf(parentId, CommonConstant.IS_LEAF);
				}
			}
		}
	}

	// ========================begin 系统下部门与人员导入
	// ==================================================================
	/**
	 * 系统部门导出
	 * 
	 * @param tenantId
	 * @param idList   需要查询部门sql的id集合
	 * @return
	 */
	@Override
	public List<SysDepartExportVo> getExportDepart(Integer tenantId, List<String> idList) {
		// 获取父级部门
		List<SysDepartExportVo> parentDepart = departMapper.getSysDepartList("", tenantId, idList);
		// 子部门
		List<SysDepartExportVo> childrenDepart = new ArrayList<>();
		// 把一级部门名称放在里面
		List<SysDepartExportVo> exportDepartVoList = new ArrayList<>();
		// 存放部门一级id避免重复
		List<String> departIdList = new ArrayList<>();
		for (SysDepartExportVo sysDepart : parentDepart) {
			// step 1.添加第一级部门
			departIdList.add(sysDepart.getId());
			sysDepart.setDepartNameUrl(sysDepart.getDepartName());
			exportDepartVoList.add(sysDepart);
			// step 2.添加自己部门路径，用/分离
			// 创建路径
			List<String> path = new ArrayList<>();
			path.add(sysDepart.getDepartName());
			// 创建子部门路径
			findSysDepartPath(sysDepart, path, tenantId, childrenDepart, departIdList, idList);
			path.clear();
		}
		exportDepartVoList.addAll(childrenDepart);
		childrenDepart.clear();
		departIdList.clear();
		return exportDepartVoList;
	}

	/**
	 * 系统部门导入
	 * 
	 * @param listSysDeparts
	 * @param errorMessageList
	 */
	@Override
	public void importSysDepart(List<SysDepartExportVo> listSysDeparts, List<String> errorMessageList) {
		int num = 0;
		int tenantId = 0;
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		}
		// 部门路径排序
		Collections.sort(listSysDeparts, new Comparator<SysDepartExportVo>() {
			@Override
			public int compare(SysDepartExportVo o1, SysDepartExportVo o2) {
				if (oConvertUtils.isNotEmpty(o1.getDepartNameUrl()) && oConvertUtils.isNotEmpty(o2.getDepartNameUrl())) {
					int oldLength = o1.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					int newLength = o2.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					return oldLength - newLength;
				} else {
					return 0;
				}
			}
		});
		// 存放部门数据的map
		Map<String, SysDepart> departMap = new HashMap<>();
		// orgCode编码长度
		int codeLength = YouBianCodeUtil.ZHANWEI_LENGTH;
		// 循环第二遍导入数据
		for (SysDepartExportVo departExportVo : listSysDeparts) {
			SysDepart sysDepart = new SysDepart();
			boolean izExport = false;
			try {
				izExport = this.addDepartByName(departExportVo.getDepartNameUrl(), departExportVo.getDepartName(), sysDepart,
						errorMessageList, tenantId, departMap, num);
			} catch (Exception e) {
				// 没有查找到parentDept
			}
			// 没有错误的时候才会导入数据
			if (izExport) {
				if (oConvertUtils.isNotEmpty(departExportVo.getOrgCode())) {
					SysDepart depart = this.baseMapper.queryCompByOrgCode(departExportVo.getOrgCode());
					if (null != depart) {
						if (oConvertUtils.isNotEmpty(sysDepart.getParentId())) {
							// 更新上级部门为叶子节点
							this.updateIzLeaf(sysDepart.getParentId(), CommonConstant.IS_LEAF);
						}
						// 部门名称已存在
						errorMessageList.add("第 " + num + " 行：记录部门名称“" + departExportVo.getDepartName() + "”部门编码重复，请检查！");
						continue;
					}
					String departNameUrl = departExportVo.getDepartNameUrl();
					// 包含/说明是多级
					if (departNameUrl.contains(SymbolConstant.SINGLE_SLASH)) {
						// 判断添加部门的规则是否和生成的一致
						if (!sysDepart.getOrgCode().equals(departExportVo.getOrgCode())) {
							if (oConvertUtils.isNotEmpty(sysDepart.getParentId())) {
								// 更新上级部门为叶子节点
								this.updateIzLeaf(sysDepart.getParentId(), CommonConstant.IS_LEAF);
							}
							// 部门名称已存在
							errorMessageList.add("第 " + num + " 行：记录部门名称“" + departExportVo.getDepartName() + "”部门编码规则不匹配，请检查！");
							continue;
						}
					}
					sysDepart.setOrgCode(departExportVo.getOrgCode());
					if (oConvertUtils.isNotEmpty(sysDepart.getParentId())) {
						// 上级
						sysDepart.setOrgType("2");
					} else {
						// 下级
						sysDepart.setOrgType("1");
					}
				} else {
					sysDepart.setOrgType(sysDepart.getOrgCode().length() / codeLength + "");
				}
				sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
				sysDepart.setDepartNameEn(departExportVo.getDepartNameEn());
				sysDepart.setDepartOrder(departExportVo.getDepartOrder());
				sysDepart.setOrgCategory(oConvertUtils.getString(departExportVo.getOrgCategory(), "1"));
				sysDepart.setMobile(departExportVo.getMobile());
				sysDepart.setFax(departExportVo.getFax());
				sysDepart.setAddress(departExportVo.getAddress());
				sysDepart.setMemo(departExportVo.getMemo());
				ImportExcelUtil.importDateSaveOne(sysDepart, ISysDepartService.class, errorMessageList, num,
						CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE);
				departMap.put(departExportVo.getDepartNameUrl(), sysDepart);
			}
			num++;
		}
	}

	/**
	 * 寻找部门路径
	 *
	 * @param departVo       部门vo
	 * @param path           部门路径
	 * @param tenantId       租户id
	 * @param childrenDepart 子部门
	 * @param departIdList   部门id集合
	 * @param idList         需要查询sql的部门id集合
	 */
	private void findSysDepartPath(SysDepartExportVo departVo, List<String> path, Integer tenantId,
			List<SysDepartExportVo> childrenDepart, List<String> departIdList, List<String> idList) {
		// step 1.查询子部门的数据
		// 获取租户id和部门父id获取的部门数据
		List<SysDepartExportVo> departList = departMapper.getSysDepartList(departVo.getId(), tenantId, idList);
		// 部门为空判断
		if (departList == null || departList.size() <= 0) {
			// 判断最后一个子部门是否已拼接
			if (!departIdList.contains(departVo.getId())) {
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH, path));
				childrenDepart.add(departVo);
			}
			return;
		}

		for (SysDepartExportVo exportDepartVo : departList) {
			// 存放子级路径
			List<String> cPath = new ArrayList<>(path);
			cPath.add(exportDepartVo.getDepartName());
			// step 2.拼接子部门路径
			if (!departIdList.contains(departVo.getId())) {
				departIdList.add(departVo.getId());
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH, path));
				childrenDepart.add(departVo);
			}
			// step 3.递归查询子路径，直到找不到为止
			findSysDepartPath(exportDepartVo, cPath, tenantId, childrenDepart, departIdList, idList);
		}
	}
	// ========================end 系统下部门与人员导入
	// ==================================================================
}
