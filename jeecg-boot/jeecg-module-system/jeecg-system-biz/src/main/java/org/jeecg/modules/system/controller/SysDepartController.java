package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.vo.SysDepartExportVo;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * * 部门表 前端控制器
 * 
 * @Author: Steve @Since： 2019-01-22
 */
@RestController
@RequestMapping("/sys/sysDepart")
@Slf4j
public class SysDepartController {

	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	public RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	@Autowired
	private RedisUtil redisUtil;

	/**
	 * * 查询部门数据
	 * 
	 * * - 查出我的部门,并以树结构数据格式响应给前端
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryMyDeptTreeList", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryMyDeptTreeList() {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			if (oConvertUtils.isNotEmpty(user.getUserIdentity())
					&& user.getUserIdentity().equals(CommonConstant.USER_IDENTITY_2)) {
				// * 当前登录人为 上级
				String departIds = user.getDepartIds();
				if (StringUtils.isNotBlank(departIds)) {
					List<SysDepartTreeModel> list = sysDepartService.queryMyDeptTreeList(departIds);
					result.setResult(list);
				}
				// * 其他情况不返回
				result.setMessage(CommonConstant.USER_IDENTITY_2.toString());
				result.setSuccess(true);
			} else {
				result.setMessage(CommonConstant.USER_IDENTITY_1.toString());
				result.setSuccess(true);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * * 查询部门数据
	 * 
	 * * - 查出所有部门,并以树结构数据格式响应给前端
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryTreeList(@RequestParam(name = "ids", required = false) String ids) {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			if (oConvertUtils.isNotEmpty(ids)) {
				List<SysDepartTreeModel> departList = sysDepartService.queryTreeList(ids);
				result.setResult(departList);
			} else {
				List<SysDepartTreeModel> list = sysDepartService.queryTreeList();
				result.setResult(list);
			}
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * * 异步查询部门list
	 * 
	 * @param parentId   父节点 异步加载时传递
	 * @param ids        前端回显是传递
	 * @param primaryKey 主键字段（id或者orgCode）
	 * @return
	 */
	@RequestMapping(value = "/queryDepartTreeSync", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryDepartTreeSync(
			@RequestParam(name = "pid", required = false) String parentId,
			@RequestParam(name = "ids", required = false) String ids,
			@RequestParam(name = "primaryKey", required = false) String primaryKey) {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			List<SysDepartTreeModel> list = sysDepartService.queryTreeListByPid(parentId, ids, primaryKey);
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.setSuccess(false);
			result.setMessage("查询失败");
		}
		return result;
	}

	/**
	 * * 获取某个部门的所有父级部门的ID
	 *
	 * @param departId 根据departId查
	 * @param orgCode  根据orgCode查，departId和orgCode必须有一个不为空
	 */
	@GetMapping("/queryAllParentId")
	public Result<JSONObject> queryParentIds(
			@RequestParam(name = "departId", required = false) String departId,
			@RequestParam(name = "orgCode", required = false) String orgCode) {
		try {
			JSONObject data;
			if (oConvertUtils.isNotEmpty(departId)) {
				// * 根据departId查询
				data = sysDepartService.queryAllParentIdByDepartId(departId);
			} else if (oConvertUtils.isNotEmpty(orgCode)) {
				// * 根据orgCode查询
				data = sysDepartService.queryAllParentIdByOrgCode(orgCode);
			} else {
				return Result.error("departId 和 orgCode 不能都为空！");
			}
			return Result.OK(data);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	/**
	 * * 添加新数据
	 * 
	 * @param sysDepart
	 * @return
	 */
	@RequiresPermissions("system:depart:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@CacheEvict(value = { CacheConstant.SYS_DEPARTS_CACHE, CacheConstant.SYS_DEPART_IDS_CACHE }, allEntries = true)
	public Result<SysDepart> add(@RequestBody SysDepart sysDepart, HttpServletRequest request) {
		Result<SysDepart> result = new Result<SysDepart>();
		String username = JwtUtil.getUserNameByToken(request);
		try {
			sysDepart.setCreateBy(username);
			sysDepartService.saveDepartData(sysDepart, username);
			result.success("添加成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("操作失败");
		}
		return result;
	}

	/**
	 * * 编辑数据
	 * 
	 * @param sysDepart
	 * @return
	 */
	@RequiresPermissions("system:depart:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	@CacheEvict(value = { CacheConstant.SYS_DEPARTS_CACHE, CacheConstant.SYS_DEPART_IDS_CACHE }, allEntries = true)
	public Result<SysDepart> edit(@RequestBody SysDepart sysDepart, HttpServletRequest request) {
		String username = JwtUtil.getUserNameByToken(request);
		sysDepart.setUpdateBy(username);
		Result<SysDepart> result = new Result<SysDepart>();
		SysDepart sysDepartEntity = sysDepartService.getById(sysDepart.getId());
		if (sysDepartEntity == null) {
			result.error500("未找到对应实体");
		} else {
			boolean ok = sysDepartService.updateDepartDataById(sysDepart, username);
			if (ok) {
				result.success("修改成功!");
			}
		}
		return result;
	}

	/**
	 * * 通过id删除
	 * 
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:depart:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@CacheEvict(value = { CacheConstant.SYS_DEPARTS_CACHE, CacheConstant.SYS_DEPART_IDS_CACHE }, allEntries = true)
	public Result<SysDepart> delete(@RequestParam(name = "id", required = true) String id) {
		Result<SysDepart> result = new Result<SysDepart>();
		SysDepart sysDepart = sysDepartService.getById(id);
		if (sysDepart == null) {
			result.error500("未找到对应实体");
		} else {
			sysDepartService.deleteDepart(id);
			result.success("删除成功!");
		}
		return result;
	}

	/**
	 * * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@RequiresPermissions("system:depart:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value = { CacheConstant.SYS_DEPARTS_CACHE, CacheConstant.SYS_DEPART_IDS_CACHE }, allEntries = true)
	public Result<SysDepart> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		Result<SysDepart> result = new Result<SysDepart>();
		if (ids == null || "".equals(ids.trim())) {
			result.error500("参数不识别！");
		} else {
			this.sysDepartService.deleteBatchWithChildren(Arrays.asList(ids.split(",")));
			result.success("删除成功!");
		}
		return result;
	}

	/**
	 * * 查询所有部门树数据
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryIdTree", method = RequestMethod.GET)
	public Result<List<DepartIdModel>> queryIdTree() {
		Result<List<DepartIdModel>> result = new Result<>();
		try {
			List<DepartIdModel> list = sysDepartService.queryDepartIdTreeList();
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * * 关键字模糊搜索相关部门树
	 * 
	 * @param keyWord
	 * @return
	 */
	@RequestMapping(value = "/searchBy", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> searchBy(
			@RequestParam(name = "keyWord", required = true) String keyWord,
			@RequestParam(name = "myDeptSearch", required = false) String myDeptSearch) {
		// * 初始化结果
		Result<List<SysDepartTreeModel>> result = new Result<List<SysDepartTreeModel>>();

		/**
		 * * 部门查询
		 * 
		 * * - myDeptSearch为1时为我的部门查询
		 * * - 登录用户为上级时查只查负责部门下数据
		 */
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String departIds = null;
		if (oConvertUtils.isNotEmpty(user.getUserIdentity())
				&& user.getUserIdentity().equals(CommonConstant.USER_IDENTITY_2)) {
			departIds = user.getDepartIds();
		}
		List<SysDepartTreeModel> treeList = this.sysDepartService.searchByKeyWord(keyWord, myDeptSearch, departIds);
		if (treeList == null || treeList.size() == 0) {
			result.setSuccess(false);
			result.setMessage("未查询匹配数据！");
			return result;
		}
		result.setResult(treeList);
		return result;
	}

	/**
	 * * 导出excel
	 *
	 * @param request
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(SysDepart sysDepart, HttpServletRequest request) {
		// * 多租户模式
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			sysDepart.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		// * Step.1 AutoPoi 导出Excel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		String selections = request.getParameter("selections");
		List<String> idList = new ArrayList<>();
		if (oConvertUtils.isNotEmpty(selections)) {
			idList = Arrays.asList(selections.split(","));
		}
		// * step.2 组装导出数据
		Integer tenantId = sysDepart == null ? null : sysDepart.getTenantId();
		List<SysDepartExportVo> sysDepartExportVos = sysDepartService.getExportDepart(tenantId, idList);
		// * 导出文件名称
		mv.addObject(NormalExcelConstants.FILE_NAME, "部门列表");
		mv.addObject(NormalExcelConstants.CLASS, SysDepartExportVo.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("导入规则：\n" +
				"1、标题为第三行，部门路径和部门名称的标题不允许修改，否则会匹配失败；第四行为数据填写范围;\n" +
				"2、部门路径用英文字符/分割，部门名称为部门路径的最后一位;\n" +
				"3、部门从一级名称开始创建，如果有同级就需要多添加一行，如研发部/研发一部;研发部/研发二部;\n" +
				"4、自定义的部门编码需要满足规则才能导入。如一级部门编码为A01,那么子部门为A01A01,同级子部门为A01A02,编码固定为三位，首字母为A-Z,后两位为数字0-99，依次递增;",
				"导出人:" + user.getRealname(), "导出信息"));
		mv.addObject(NormalExcelConstants.DATA_LIST, sysDepartExportVos);

		return mv;
	}

	/**
	 * * 通过excel导入数据
	 * 
	 * * - 部门导入方案1: 通过机构编码来计算出部门的父级ID,维护上下级关系;
	 * * - 部门导入方案2: 你也可以改造下程序,机构编码直接导入,先不设置父ID;全部导入后,写一个sql,补下父ID;
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("system:depart:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	@CacheEvict(value = { CacheConstant.SYS_DEPARTS_CACHE, CacheConstant.SYS_DEPART_IDS_CACHE }, allEntries = true)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<String> errorMessageList = new ArrayList<>();
		List<SysDepartExportVo> listSysDeparts = null;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			// * 获取上传文件对象
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);

			try {
				// * 系统的部门导入导出也可以改成敲敲云模式的部门路径
				listSysDeparts = ExcelImportUtil.importExcel(file.getInputStream(), SysDepartExportVo.class, params);
				sysDepartService.importSysDepart(listSysDeparts, errorMessageList);

				// * 清空部门缓存
				List<String> keys3 = redisUtil.scan(CacheConstant.SYS_DEPARTS_CACHE + "*");
				List<String> keys4 = redisUtil.scan(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
				redisTemplate.delete(keys3);
				redisTemplate.delete(keys4);
				return ImportExcelUtil.imporReturnRes(errorMessageList.size(), listSysDeparts.size() - errorMessageList.size(),
						errorMessageList);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Result.error("文件导入失败:" + e.getMessage());
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Result.error("文件导入失败！");
	}

	/**
	 * * 查询所有部门信息
	 * 
	 * @return
	 */
	@GetMapping("listAll")
	public Result<List<SysDepart>> listAll(@RequestParam(name = "id", required = false) String id) {
		Result<List<SysDepart>> result = new Result<>();
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.orderByAsc(SysDepart::getOrgCode);
		if (oConvertUtils.isNotEmpty(id)) {
			String[] arr = id.split(",");
			query.in(SysDepart::getId, Arrays.asList(arr));
		}
		List<SysDepart> ls = this.sysDepartService.list(query);
		result.setSuccess(true);
		result.setResult(ls);
		return result;
	}

	/**
	 * * 查询数据
	 * 
	 * * 关键字 查出所有部门
	 * * 关键字 查出用户
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryTreeByKeyWord", method = RequestMethod.GET)
	public Result<Map<String, Object>> queryTreeByKeyWord(
			@RequestParam(name = "keyWord", required = false) String keyWord) {
		Result<Map<String, Object>> result = new Result<>();
		try {
			Map<String, Object> map = new HashMap<>(5);
			// * 获取树
			List<SysDepartTreeModel> list = sysDepartService.queryTreeByKeyWord(keyWord);

			// * 根据keyWord获取用户信息
			LambdaQueryWrapper<SysUser> queryUser = new LambdaQueryWrapper<SysUser>();
			queryUser.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_0);
			queryUser.and(i -> i.like(SysUser::getUsername, keyWord).or().like(SysUser::getRealname, keyWord));
			List<SysUser> sysUsers = this.sysUserService.list(queryUser);
			map.put("userList", sysUsers);
			map.put("departList", list);
			result.setResult(map);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * * 根据部门编码获取部门信息
	 *
	 * @param orgCode
	 * @return
	 */
	@GetMapping("/getDepartName")
	public Result<SysDepart> getDepartName(@RequestParam(name = "orgCode") String orgCode) {
		Result<SysDepart> result = new Result<>();
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
		query.eq(SysDepart::getOrgCode, orgCode);
		SysDepart sysDepart = sysDepartService.getOne(query);
		result.setSuccess(true);
		result.setResult(sysDepart);
		return result;
	}

	/**
	 * * 根据部门id获取用户信息
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/getUsersByDepartId")
	public Result<List<SysUser>> getUsersByDepartId(@RequestParam(name = "id") String id) {
		Result<List<SysUser>> result = new Result<>();
		List<SysUser> sysUsers = sysUserDepartService.queryUserByDepId(id);
		result.setSuccess(true);
		result.setResult(sysUsers);
		return result;
	}

	/**
	 * * 批量查询
	 * 
	 * @param deptIds
	 * @return
	 */
	@RequestMapping(value = "/queryByIds", method = RequestMethod.GET)
	public Result<Collection<SysDepart>> queryByIds(@RequestParam(name = "deptIds") String deptIds) {
		Result<Collection<SysDepart>> result = new Result<>();
		String[] ids = deptIds.split(",");
		Collection<String> idList = Arrays.asList(ids);
		Collection<SysDepart> deptList = sysDepartService.listByIds(idList);
		result.setSuccess(true);
		result.setResult(deptList);
		return result;
	}

	/**
	 * * 获取我的部门已加入的公司
	 */
	@GetMapping("/getMyDepartList")
	public Result<List<SysDepart>> getMyDepartList() {
		List<SysDepart> list = sysDepartService.getMyDepartList();
		return Result.ok(list);
	}

	/**
	 * * 异步查询部门list
	 * 
	 * @param parentId 父节点 异步加载时传递
	 * @return
	 */
	@RequestMapping(value = "/queryBookDepTreeSync", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryBookDepTreeSync(
			@RequestParam(name = "pid", required = false) String parentId,
			@RequestParam(name = "tenantId") Integer tenantId,
			@RequestParam(name = "departName", required = false) String departName) {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			List<SysDepartTreeModel> list = sysDepartService.queryBookDepTreeSync(parentId, tenantId, departName);
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * * 通过部门id和租户id获取用户
	 * 
	 * * - 低代码应用: 用于选择部门负责人
	 * 
	 * @param departId
	 * @return
	 */
	@GetMapping("/getUsersByDepartTenantId")
	public Result<List<SysUser>> getUsersByDepartTenantId(@RequestParam("departId") String departId) {
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		List<SysUser> sysUserList = sysUserDepartService.getUsersByDepartTenantId(departId, tenantId);
		return Result.ok(sysUserList);
	}

	/**
	 * * 导出excel
	 * 
	 * * - 低代码应用: 用于导出部门
	 *
	 * @param request
	 */
	@RequestMapping(value = "/appExportXls")
	public ModelAndView appExportXls(SysDepart sysDepart, HttpServletRequest request) {
		// * Step.1 组装查询条件
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<ExportDepartVo> pageList = sysDepartService.getExcelDepart(tenantId);
		// * Step.2 AutoPoi 导出Excel
		mv.addObject(NormalExcelConstants.FILE_NAME, "部门列表");
		mv.addObject(NormalExcelConstants.CLASS, ExportDepartVo.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("部门列表数据", "导出人:" + user.getRealname(), "导出信息"));
		mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
		return mv;
	}

	/**
	 * * 导入excel
	 * 
	 * * - 低代码应用: 用于导入部门
	 *
	 * @param request
	 */
	@RequestMapping(value = "/appImportExcel", method = RequestMethod.POST)
	@CacheEvict(value = { CacheConstant.SYS_DEPARTS_CACHE, CacheConstant.SYS_DEPART_IDS_CACHE }, allEntries = true)
	public Result<?> appImportExcel(HttpServletRequest request, HttpServletResponse response) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<String> errorMessageList = new ArrayList<>();
		List<ExportDepartVo> listSysDeparts = null;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			// * 获取上传文件对象
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				listSysDeparts = ExcelImportUtil.importExcel(file.getInputStream(), ExportDepartVo.class, params);
				sysDepartService.importExcel(listSysDeparts, errorMessageList);
				// * 清空部门缓存
				List<String> keys3 = redisUtil.scan(CacheConstant.SYS_DEPARTS_CACHE + "*");
				List<String> keys4 = redisUtil.scan(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
				redisTemplate.delete(keys3);
				redisTemplate.delete(keys4);
				return ImportExcelUtil.imporReturnRes(errorMessageList.size(), listSysDeparts.size() - errorMessageList.size(),
						errorMessageList);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Result.error("文件导入失败:" + e.getMessage());
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Result.error("文件导入失败！");
	}

}
