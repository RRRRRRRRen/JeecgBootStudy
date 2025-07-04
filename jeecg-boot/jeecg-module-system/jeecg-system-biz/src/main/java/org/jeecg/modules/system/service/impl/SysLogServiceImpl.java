package org.jeecg.modules.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * * 依赖注入
 */
import javax.annotation.Resource;

import com.baomidou.mybatisplus.annotation.DbType;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.modules.system.entity.SysLog;
import org.jeecg.modules.system.mapper.SysLogMapper;
import org.jeecg.modules.system.service.ISysLogService;
/**
 * * 标记一个类为 服务组件
 */
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * * 系统日志表 服务实现类
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements ISysLogService {

	/**
	 * * 注入sql依赖
	 */
	@Resource
	private SysLogMapper sysLogMapper;

	@Override
	public void removeAll() {
		sysLogMapper.removeAll();
	}

	@Override
	public Long findTotalVisitCount() {
		return sysLogMapper.findTotalVisitCount();
	}

	@Override
	public Long findTodayVisitCount(Date dayStart, Date dayEnd) {
		return sysLogMapper.findTodayVisitCount(dayStart, dayEnd);
	}

	@Override
	public Long findTodayIp(Date dayStart, Date dayEnd) {
		return sysLogMapper.findTodayIp(dayStart, dayEnd);
	}

	@Override
	public List<Map<String, Object>> findVisitCount(Date dayStart, Date dayEnd) {
		// * 获取数据库类型
		DbType dbType = CommonUtils.getDatabaseTypeEnum();
		return sysLogMapper.findVisitCount(dayStart, dayEnd, dbType.getDb());
	}
}
