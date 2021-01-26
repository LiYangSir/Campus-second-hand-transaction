package com.quguai.campustransaction.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ?Ż?ȯ??Ϣ
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:42:04
 */
@Data
@TableName("sms_coupon")
public class CouponEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;

	private Integer couponType;

	private String couponImg;

	private String couponName;

	private Integer num;
	/**
	 * ?
	 */
	private BigDecimal amount;
	/**
	 * ÿ?????????
	 */
	private Integer perLimit;
	/**
	 * ʹ???ż
	 */
	private BigDecimal minPoint;
	/**
	 * ??ʼʱ?
	 */
	private Date startTime;

	private Date endTime;

	private Integer useType;

	private String note;
	/**
	 * ?????
	 */
	private Integer publishCount;
	/**
	 * ??ʹ???
	 */
	private Integer useCount;
	/**
	 * ??ȡ?
	 */
	private Integer receiveCount;
	/**
	 * ??????ȡ?Ŀ?ʼ???
	 */
	private Date enableStartTime;
	/**
	 * ??????ȡ?Ľ??????
	 */
	private Date enableEndTime;
	/**
	 * ?Ż??
	 */
	private String code;
	/**
	 * ??????ȡ?Ļ?Ա?ȼ?[0->???޵ȼ???????-??Ӧ?ȼ?]
	 */
	private Integer memberLevel;
	/**
	 * ????״̬[0-δ??????1-?ѷ???]
	 */
	private Integer publish;

}
