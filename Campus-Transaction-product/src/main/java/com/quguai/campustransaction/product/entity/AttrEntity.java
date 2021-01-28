package com.quguai.campustransaction.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ??Ʒ?
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
@Data
@TableName("pms_attr")
public class AttrEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId
	private Long attrId;

	private String attrName;
	private Integer searchType;
	private Integer valueType;
	private String icon;
	private String valueSelect;
	private Integer attrType;
	private Long enable;
	private Long catelogId;
	private Integer showDesc;

}
