package com.quguai.campustransaction.product.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spu??Ϣ
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:29
 */
@Data
@TableName("pms_spu_info")
public class SpuInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private String spuName;
	private String spuDescription;
	private Long catalogId;
	private Long brandId;
	private BigDecimal weight;
	private Integer publishStatus;

	@TableField(fill = FieldFill.INSERT)
	private Date createTime;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;

}
