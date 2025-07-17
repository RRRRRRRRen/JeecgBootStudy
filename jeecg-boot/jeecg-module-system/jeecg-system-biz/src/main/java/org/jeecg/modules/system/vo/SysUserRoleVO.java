package org.jeecg.modules.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * * 用户角色vo
 * 
 * @Description: 用户角色vo
 * @author: jeecg-boot
 */
@Data
public class SysUserRoleVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * * 角色id
	 */
	private String roleId;
	/**
	 * * 对应的用户id集合
	 */
	private List<String> userIdList;

	/**
	 * * 没有必要主动调用父类无参构造器
	 */
	public SysUserRoleVO() {
		super();
	}

	public SysUserRoleVO(String roleId, List<String> userIdList) {
		super();
		this.roleId = roleId;
		this.userIdList = userIdList;
	}

}
