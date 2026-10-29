package org.health.config;

import org.health.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获业务自定义异常（登录失败、用户名重复等）
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e){
        e.printStackTrace();
        return Result.error(e.getMessage());
    }

    // 捕获其他全部未知异常
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e){
        e.printStackTrace();
        return Result.error("服务器繁忙，请稍后重试");
    }
}
