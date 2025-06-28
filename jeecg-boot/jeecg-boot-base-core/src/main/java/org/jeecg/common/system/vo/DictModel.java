package org.jeecg.common.system.vo;

import java.io.Serializable;

/**
 * * 1. 将 Java 对象转换为 JSON 字符串： 可以将 Java 对象（如 POJO、Map 或 List）序列化为 JSON 格式的字符串。
 * * 2. 将 JSON 字符串解析为 Java 对象： 可以将 JSON 字符串反序列化为对应的 Java 对象。
 * * 3. 操作 JSON 数据： 提供了灵活的方法来创建、修改和查询 JSON 数据。
 */
import com.alibaba.fastjson.JSONObject;
/**
 * * 注解用于指定在将 Java 对象转换为 JSON 字符串（序列化）或从 JSON 字符串转换为 Java 对象（反序列化）时忽略的属性（字段）。
 * * 它可以帮助你控制哪些字段不应该被包含在 JSON 输出中，或者在反序列化时不应该被设置。
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * * 自动生成常用的 getter、setter、toString、equals 和 hashCode 方法，简化代码。
 */
import lombok.Data;
/**
 * * 扩展 equals 和 hashCode 方法支持 callSuper 属性
 */
import lombok.EqualsAndHashCode;
/**
 * * 支持链式调用（如 setXxx().setYyy()）
 */
import lombok.experimental.Accessors;

/**
 * @Description: 字典类
 * @author: jeecg-boot
 */
@Data
/**
 * * 不考虑父类字段的比较
 */
@EqualsAndHashCode(callSuper = false)
/**
 * * 开启链式调用
 */
@Accessors(chain = true)
/**
 * * 反序列化时忽略未知属性
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictModel implements Serializable {
	/**
	 * * 序列化ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * * 无参构造函数
	 */
	public DictModel() {
	}

	/**
	 * * 带参数的构造函数
	 * 
	 * @param value 字典值
	 * @param text  字典文本
	 */
	public DictModel(String value, String text) {
		this.value = value;
		this.text = text;
	}

	/**
	 * * 带参数的构造函数
	 * 
	 * @param value 字典值
	 * @param text  字典文本
	 * @param color 字典颜色
	 */
	public DictModel(String value, String text, String color) {
		this.value = value;
		this.text = text;
		this.color = color;
	}

	/**
	 * * 字典value
	 */
	private String value;
	/**
	 * * 字典文本
	 */
	private String text;
	/**
	 * * 字典颜色
	 */
	private String color;

	/**
	 * * 获取字典文本
	 * * 特殊用途： JgEditableTable
	 * 
	 * @return
	 */
	public String getTitle() {
		return this.text;
	}

	/**
	 * * 获取字典文本
	 * * 特殊用途： vue3 Select组件
	 */
	public String getLabel() {
		return this.text;
	}

	/**
	 * TODO 未知的使用方法
	 * * 用于表单设计器 关联记录表数据存储
	 * 
	 * * 特殊用途： 存储与字典相关的额外结构化数据（如扩展属性、动态配置等）。
	 * * 可通过 JSONObject 提供的方法灵活操作键值对。
	 */
	private JSONObject jsonObject;

}
