package com.quguai.campustransaction.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ?Ż?ȯ??ȡ??ʷ??¼
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:42:04
 */
@Data
@TableName("sms_coupon_history")
public class CouponHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * ?Ż?ȯid
	 */
	private Long couponId;
	/**
	 * ??Աid
	 */
	private Long memberId;
	/**
	 * ??Ա?
	 */
	private String memberNickName;
	/**
	 * ??ȡ??ʽ[0->??̨???ͣ?1->??????ȡ]
	 */
	private Integer getType;
	/**
	 * ????ʱ?
	 */
	private Date createTime;
	/**
	 * ʹ??״̬[0->δʹ?ã?1->??ʹ?ã?2->?ѹ???]
	 */
	private Integer useType;
	/**
	 * ʹ??ʱ?
	 */
	private Date useTime;
	/**
	 * ????id
	 */
	private Long orderId;
	/**
	 * ?????
	 */
	private Long orderSn;

}
