/**
  * Copyright 2021 bejson.com 
  */
package com.quguai.campustransaction.product.vo.spu;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class Bounds {

    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}