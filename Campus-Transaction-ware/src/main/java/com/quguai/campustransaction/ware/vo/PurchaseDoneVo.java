package com.quguai.campustransaction.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;

    private List<PurchaseDoneItemVo> items;

}
