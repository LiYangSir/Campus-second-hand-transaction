package com.quguai.common.exception;

/**
 * 10: 通用模块
 *    001：参数格式
 *    002： 短信验证吗
 * 11： 商品
 * 12： 购物车
 * 13： 订单
 * 14： 物流
 * 15： 用户
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_SMS_CODE(10002, "短信验证码获取频繁"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架出现错误"),
    USERNAME_EXIST_EXCEPTION(15001, "用户名已经存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号码已经存在"),
    NO_STOCK_EXCEPTION(21000, "商品库存不足"),
    LOGIN_ACCOUNT_PASSWORD_ERROR_EXCEPTION(15003, "账号或者密码错误");

    private int code;
    private String message;

    private BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
