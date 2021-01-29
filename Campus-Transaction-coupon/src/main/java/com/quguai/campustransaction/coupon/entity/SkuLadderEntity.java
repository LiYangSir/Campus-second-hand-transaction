package com.quguai.campustransaction.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ??Ʒ???ݼ۸
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:42:04
 */
@Data
@TableName("sms_sku_ladder")
public class SkuLadderEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Long skuId;
	private Integer fullCount;
	private BigDecimal discount;
	private BigDecimal price;
	private Integer addOther;

}
