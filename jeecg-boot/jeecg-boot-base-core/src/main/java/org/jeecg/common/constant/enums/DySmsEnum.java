package org.jeecg.common.constant.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * * 短信模版枚举
 * 
 * @Description: 短信枚举类
 * @author: jeecg-boot
 */
public enum DySmsEnum {

	/**
	 * * 登录 短信模板编码
	 */
	LOGIN_TEMPLATE_CODE("SMS_175435174", "敲敲云", "code"),
	/**
	 * * 忘记密码 短信模板编码
	 */
	FORGET_PASSWORD_TEMPLATE_CODE("SMS_175435174", "敲敲云", "code"),
	/**
	 * * 修改密码 短信模板编码
	 */
	CHANGE_PASSWORD_TEMPLATE_CODE("SMS_465391221", "敲敲云", "code"),
	/**
	 * * 注册账号 短信模板编码
	 */
	REGISTER_TEMPLATE_CODE("SMS_175430166", "敲敲云", "code");

	/**
	 * * 短信模板编码
	 */
	private String templateCode;
	/**
	 * * 签名
	 */
	private String signName;
	/**
	 * * 短信模板必需的数据名称，多个key以逗号分隔，此处配置作为校验
	 */
	private String keys;

	/**
	 * * 构造器
	 * 
	 * @param templateCode
	 * @param signName
	 * @param keys
	 */
	private DySmsEnum(String templateCode, String signName, String keys) {
		this.templateCode = templateCode;
		this.signName = signName;
		this.keys = keys;
	}

	public String getTemplateCode() {
		return templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	public String getSignName() {
		return signName;
	}

	public void setSignName(String signName) {
		this.signName = signName;
	}

	public String getKeys() {
		return keys;
	}

	public void setKeys(String keys) {
		this.keys = keys;
	}

	/**
	 * * 根据模板代码转换为相应的DySmsEnum枚举值
	 * 
	 * @param templateCode 短信模板代码，用于识别特定的短信模板
	 * @return 对应模板代码的DySmsEnum枚举值，如果找不到匹配项则返回null
	 */
	public static DySmsEnum toEnum(String templateCode) {
		// * 检查输入的模板代码是否为空，为空则直接返回null
		if (StringUtils.isEmpty(templateCode)) {
			return null;
		}
		// * 遍历DySmsEnum的所有值，寻找匹配的模板代码
		for (DySmsEnum item : DySmsEnum.values()) {
			// 如果找到匹配的模板代码，返回对应的枚举值
			if (item.getTemplateCode().equals(templateCode)) {
				return item;
			}
		}
		// * 如果没有找到匹配的模板代码，返回null
		return null;
	}
}
