package com.quguai.campustransaction.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;
import sun.rmi.runtime.Log;

/**
 * ???Է??
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long attrGroupId;
	private String attrGroupName;
	private Integer sort;
	private String descript;
	private String icon;
	private Long catelogId;

	@TableField(exist = false)
	private List<Long> catelogPath;

}
