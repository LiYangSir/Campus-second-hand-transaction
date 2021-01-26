package com.quguai.campustransaction.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.quguai.common.valid.AddGroup;
import com.quguai.common.valid.ListValue;
import com.quguai.common.valid.UpdateGroup;
import com.quguai.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;


/**
 * Ʒ?
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
@Data
@TableName("pms_brand")
@Validated
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 注意如果指定了group @Validated(value = {UpdateGroup.class})那么只会触发带group的字段，不带的不会进行验证
	 * 如果没有指定的@Validated也只会触发不带group属性的，类似创建一个默认分组
	 * 自定义校验器
	 */

	@Null(message = "新增不能指定ID", groups = {AddGroup.class})
	@NotNull(message = "修改必须指定品牌ID", groups = {UpdateGroup.class, UpdateStatusGroup.class})
	@TableId
	private Long brandId;

	@NotBlank(message = "品牌名称不允许为空", groups = {AddGroup.class, UpdateGroup.class})
	private String name;

	@NotBlank(message = "URL不能为空", groups = {AddGroup.class})
	@URL(message = "logo必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;

	private String descript;

	@NotNull(message = "显示状态不能为空", groups = {AddGroup.class, UpdateGroup.class, UpdateStatusGroup.class})
	@ListValue(vals = {0, 1}, groups = {AddGroup.class, UpdateGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;

	@NotBlank(message = "首字母不能为空", groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;

	@NotNull(message = "排序字段不能为空", groups = {AddGroup.class})
	@Min(value = 0, message = "排序必须大于等于0", groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
