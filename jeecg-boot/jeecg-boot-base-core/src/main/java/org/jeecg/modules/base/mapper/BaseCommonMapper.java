package org.jeecg.modules.base.mapper;

/**
 * * 在指定方法或类上，临时关闭某些 MyBatis-Plus 内置拦截器的效果。
 */
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
/**
 * * 给 Mapper 接口方法里的参数指定名称，方便 SQL 映射和多参数传递。
 */
import org.apache.ibatis.annotations.Param;
import org.jeecg.common.api.dto.LogDTO;

/**
 * * 日志
 * 
 * @Description: BaseCommonMapper
 * @author: jeecg-boot
 */
public interface BaseCommonMapper {

    /**
     * * 保存日志
     * 
     * @param dto
     */
    @InterceptorIgnore(illegalSql = "true", tenantLine = "true")
    void saveLog(@Param("dto") LogDTO dto);

}
