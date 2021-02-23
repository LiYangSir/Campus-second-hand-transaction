/**
  * Copyright 2021 bejson.com 
  */
package com.quguai.campustransaction.product.vo.spu;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class Attr {
    private Long attrId;
    private String attrName;
    private String attrValue;

}