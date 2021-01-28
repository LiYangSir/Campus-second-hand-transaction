package com.quguai.campustransaction.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class AttrResponseVo extends AttrVo {

    private String groupName;
    private String catelogName;

    private List<Long> catelogPath;
}
