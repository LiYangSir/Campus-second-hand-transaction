package com.quguai.campustransaction.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * sku??Ϣ
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
@Data
@TableName("pms_sku_info")
public class SkuInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId
	private Long skuId;

	private Long spuId;
	private String skuName;
	private String skuDesc;
	private Long catalogId;
	private Long brandId;
	private String skuDefaultImg;
	private String skuTitle;
	private String skuSubtitle;
	private BigDecimal price;
	private Long saleCount;

}
