package com.quguai.campustransaction.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spu??Ϣ???
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:29
 */
@Data
@TableName("pms_spu_info_desc")
public class SpuInfoDescEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.INPUT)
	private Long spuId;
	private String decript;

}
