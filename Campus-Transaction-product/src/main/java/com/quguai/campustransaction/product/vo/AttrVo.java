package com.quguai.campustransaction.product.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AttrVo {

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

    private Long attrGroupId;
}
