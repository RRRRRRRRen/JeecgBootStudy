package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.mapper.SysPositionMapper;
import org.jeecg.modules.system.service.ISysPositionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * * 职务表
 * 
 * @Description: 职务表
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
@Service
public class SysPositionServiceImpl extends ServiceImpl<SysPositionMapper, SysPosition> implements ISysPositionService {

    /**
     * * 通过code查询
     * 
     * @param code 职务编码
     * @return SysPosition
     */
    @Override
    public SysPosition getByCode(String code) {
        LambdaQueryWrapper<SysPosition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPosition::getCode, code);
        return super.getOne(queryWrapper);
    }

    /**
     * * 通过用户id获取职位名称列表
     * 
     * @param userId
     * @return
     */
    @Override
    public List<SysPosition> getPositionList(String userId) {
        return this.baseMapper.getPositionList(userId);
    }

    /**
     * * 获取职位名称
     * 
     * @param postList
     * @return
     */
    @Override
    public String getPositionName(List<String> postList) {
        List<SysPosition> positionNameList = this.baseMapper.getPositionName(postList);
        if (null != positionNameList && positionNameList.size() > 0) {
            return positionNameList.stream().map(SysPosition::getName)
                    .collect(Collectors.joining(SymbolConstant.COMMA));
        }
        return "";
    }
}
