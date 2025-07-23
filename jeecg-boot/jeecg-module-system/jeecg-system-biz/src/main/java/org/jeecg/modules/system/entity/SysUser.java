package org.jeecg.modules.system.entity;

import java.util.Date;

/**
 * * 作用
 * 
 * * 1.字段名映射
 * * - 字段名与数据库表字段名不一致时使用
 * * - 例如：@TableField("user_name")
 * 
 * * 2.是否存在于数据库表中
 * * - 例如：@TableField(exist = false)
 * 
 * * 3.自动填充字段（插入/更新）
 * * - 例如：@TableField(fill = FieldFill.INSERT)
 */
import com.baomidou.mybatisplus.annotation.TableField;
/**
 * * 用于实现逻辑删除功能
 * * 即：不真正删除数据库记录，而是通过标记字段表示已删除，常用于保留历史数据、审计等场景。
 * 
 * * - 当你执行 deleteById()、remove() 等删除操作时：
 * * - 实际执行的是 updateById() 操作，将 del_flag 字段设置为 1。
 * * - 例如：@TableLogic(value = "del_flag", delval = "1")
 */
import com.baomidou.mybatisplus.annotation.TableLogic;
/**
 * * @JsonProperty 注解用于配置 JSON 序列化时的字段映射。
 * 
 * * - 使用 @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) 注解
 * * - 这个字段只能反序列化（接收 JSON 数据时用），不会被序列化（返回 JSON 时不会输出）。
 */
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * * @Dict 注解用于配置字典类型。
 * * - 使用 @Dict(dicCode = "sex") 注解，可以指定字典类型。
 */
import org.jeecg.common.aspect.annotation.Dict;
/**
 * * @Excel 注解用于配置 Excel 导入导出时的字段映射。
 */
import org.jeecgframework.poi.excel.annotation.Excel;
/**
 * * @DateTimeFormat 注解用于配置日期格式。
 * 
 * * 用于将字符串格式的日期（例如请求参数、表单数据、URL参数等）自动转换为 Java 的 Date 或 LocalDateTime 类型。
 */
import org.springframework.format.annotation.DateTimeFormat;

/**
 * * 这是用于标注实体类中哪个字段是主键的注解同时
 * * 也可以通过它指定主键生成策略。
 * 
 * * - 使用 @TableId(type = IdType.ASSIGN_ID) 注解，可以指定主键生成策略。
 * * - IdType.ASSIGN_ID 分布式全局唯一 ID（默认使用雪花算法）→ 推荐
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
/**
 * * @JsonFormat 注解用于配置 JSON 序列化时的日期格式。
 * 
 * * @JsonFormat 主要用于 JSON 的处理（比如 @RequestBody 和 @ResponseBody）；
 */
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;

/**
 * * @Data 注解用于生成 getter 和 setter 方法，以及 toString、equals、hashCode 方法。
 * * 启用后，可以自动生成 getter 和 setter 方法，以及 toString、equals、hashCode 方法，而不需要手动实现。
 */
import lombok.Data;
/**
 * * EqualsAndHashCode(callSuper = false) 注解用于生成 equals 和 hashCode 方法。
 * * 启用后，可以自动生成 equals 和 hashCode 方法，而不需要手动实现。
 */
import lombok.EqualsAndHashCode;
/**
 * * Accessors(chain = true) 注解用于启用链式调用。
 * * 启用后，可以连续调用对象的多个方法，而不需要每次都创建新的对象。
 */
import lombok.experimental.Accessors;

/**
 * * 用户表
 *
 * @Author scott
 * @since 2018-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysUser implements Serializable {

    // * 序列化版本号
    private static final long serialVersionUID = 1L;

    /**
     * * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * * 登录账号
     */
    @Excel(name = "登录账号", width = 15)
    private String username;

    /**
     * * 真实姓名
     */
    @Excel(name = "真实姓名", width = 15)
    private String realname;

    /**
     * * 密码
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * * md5密码盐
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String salt;

    /**
     * * 头像
     */
    @Excel(name = "头像", width = 15, type = 2)
    private String avatar;

    /**
     * * 生日
     */
    @Excel(name = "生日", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * * 性别（1：男 2：女）
     */
    @Excel(name = "性别", width = 15, dicCode = "sex")
    @Dict(dicCode = "sex")
    private Integer sex;

    /**
     * * 电子邮件
     */
    @Excel(name = "电子邮件", width = 15)
    private String email;

    /**
     * * 电话
     */
    @Excel(name = "电话", width = 15)
    private String phone;

    /**
     * * 登录选择部门编码
     */
    private String orgCode;
    /**
     * * 登录选择租户ID
     */
    private Integer loginTenantId;

    /**
     * * 部门名称
     */
    private transient String orgCodeTxt;

    /**
     * * 状态(1：正常 2：冻结 ）
     */
    @Excel(name = "状态", width = 15, dicCode = "user_status")
    @Dict(dicCode = "user_status")
    private Integer status;

    /**
     * * 删除状态（0，正常，1已删除）
     */
    @Excel(name = "删除状态", width = 15, dicCode = "del_flag")
    @TableLogic
    private Integer delFlag;

    /**
     * * 工号，唯一键
     */
    @Excel(name = "工号", width = 15)
    private String workNo;

    /**
     * * 职务，关联职务表
     */
    @Excel(name = "职务", width = 15)
    @Dict(dictTable = "sys_position", dicText = "name", dicCode = "id")
    @TableField(exist = false)
    private String post;

    /**
     * * 座机号
     */
    @Excel(name = "座机号", width = 15)
    private String telephone;

    /**
     * * 创建人
     */
    private String createBy;

    /**
     * * 创建时间
     */
    private Date createTime;

    /**
     * * 更新人
     */
    private String updateBy;

    /**
     * * 更新时间
     */
    private Date updateTime;
    /**
     * * 同步工作流引擎1同步0不同步
     */
    private Integer activitiSync;

    /**
     * * 身份（1 普通成员 2 上级）
     */
    @Excel(name = "（1普通成员 2上级）", width = 15)
    private Integer userIdentity;

    /**
     * * 负责部门
     */
    @Excel(name = "负责部门", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    private String departIds;

    /**
     * * 多租户ids临时用，不持久化数据库(数据库字段不存在)
     */
    @TableField(exist = false)
    private String relTenantIds;

    /**
     * * 设备id uniapp推送用
     */
    private String clientId;

    /**
     * * 登录首页地址
     */
    @TableField(exist = false)
    private String homePath;

    /**
     * * 职位名称
     */
    @TableField(exist = false)
    private String postText;

    /**
     * * 流程状态
     */
    private String bpmStatus;

    /**
     * * 是否已经绑定第三方
     */
    @TableField(exist = false)
    private boolean izBindThird;
}
