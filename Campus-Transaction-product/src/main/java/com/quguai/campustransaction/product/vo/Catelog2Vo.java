package com.quguai.campustransaction.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog2Vo {
    private String catalog1Id;
    private String id;
    private String name;
    private List<Catelog3Vo> catalog3List;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catelog3Vo {
        private String catalog2Id;
        private String id;
        private String name;
    }
}
