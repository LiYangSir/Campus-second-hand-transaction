package com.quguai.common.constant;

import lombok.AllArgsConstructor;

public class WareConstant {

    @AllArgsConstructor
    public enum PurchaseStatusEnum{
        CREATE_STATUS(0, "新建"),
        ASSIGNED(1, "已分配"),
        RECEIVE(2, "已领取"),
        FINISH(3, "已完成"),
        HAS_ERROR(4, "有异常");

        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    @AllArgsConstructor
    public enum PurchaseDetailStatusEnum{
        CREATE_STATUS(0, "新建"),
        ASSIGNED(1, "已分配"),
        BUYING(2, "整在采购"),
        FINISH(3, "采购完成"),
        HAS_ERROR(4, "采购失败");

        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
