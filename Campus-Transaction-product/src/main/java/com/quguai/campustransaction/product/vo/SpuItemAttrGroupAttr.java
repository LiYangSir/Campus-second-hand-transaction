package com.quguai.campustransaction.product.vo;

import com.quguai.campustransaction.product.vo.spu.Attr;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SpuItemAttrGroupAttr {
    private String groupName;
    List<Attr> attrs;
}
