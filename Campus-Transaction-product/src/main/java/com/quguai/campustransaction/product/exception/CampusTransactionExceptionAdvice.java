package com.quguai.campustransaction.product.exception;

import com.quguai.common.exception.BizCodeEnum;
import com.quguai.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.quguai.campustransaction.product.controller")
public class CampusTransactionExceptionAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handlerValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现错误；{}，异常类型：{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> map = new HashMap<>();
        bindingResult.getFieldErrors().forEach(item -> {
            map.put(item.getField(), item.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION).put("data", map);
    }

    @ExceptionHandler(Throwable.class)
    public R handleException(Throwable e) {
        log.error("任意出现错误；{}，异常类型：{}", e.getMessage(), e.getClass());
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION);
    }
}
