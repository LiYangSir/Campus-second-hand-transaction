package com.quguai.campustransaction.member.exception;

public class PhoneNumberExistException extends RuntimeException {
    public PhoneNumberExistException() {
        super("手机号码已经存在");
    }
}
