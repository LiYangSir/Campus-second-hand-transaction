package com.quguai.campustransaction.product.vo;

import com.quguai.campustransaction.product.entity.AttrEntity;
import com.quguai.campustransaction.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrsVo {

    private Long attrGroupId;
    private String attrGroupName;
    private Integer sort;
    private String descript;
    private String icon;
    private Long catelogId;
    private List<AttrEntity> attrs;
}
