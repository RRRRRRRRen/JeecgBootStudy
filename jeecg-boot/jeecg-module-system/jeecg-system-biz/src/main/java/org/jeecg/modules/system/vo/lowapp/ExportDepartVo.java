package org.jeecg.modules.system.vo.lowapp;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * * 部门导出模型
 */
@Data
public class ExportDepartVo {
    /**
     * * 部门路径
     */
    @Excel(name = "部门路径", width = 50)
    private String departNameUrl;

    /**
     * * 机构/部门名称
     */
    @Excel(name = "部门名称", width = 50)

    private String departName;
    /**
     * * id
     */
    private String id;

    /**
     * * 父级id
     */
    private String parentId;

}
