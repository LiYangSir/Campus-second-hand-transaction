package com.quguai.campustransaction.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ??Ʒ??Ա?۸
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:42:04
 */
@Data
@TableName("sms_member_price")
public class MemberPriceEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Long skuId;
	private Long memberLevelId;
	private String memberLevelName;
	private BigDecimal memberPrice;
	private Integer addOther;

}
