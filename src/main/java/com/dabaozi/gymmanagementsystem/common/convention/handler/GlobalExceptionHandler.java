package com.dabaozi.gymmanagementsystem.common.convention.handler;

import com.dabaozi.gymmanagementsystem.common.convention.errorcode.BaseErrorCode;
import com.dabaozi.gymmanagementsystem.common.convention.exception.AbstractException;
import com.dabaozi.gymmanagementsystem.common.convention.exception.BaseException;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器,把异常统一转成 Result 结构返回
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * AbstractException 体系:ClientException/ServiceException/RemoteException
     */
    @ExceptionHandler(AbstractException.class)
    public Result<Void> handleAbstract(AbstractException ex) {
        log.warn("业务异常:{}", ex.toString());
        return Results.failure(ex);
    }

    /**
     * BaseException 体系:AccountNotFound/AccountLocked/PasswordError(登录在用)
     */
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBase(BaseException ex) {
        log.warn("账号异常:{}", ex.getMessage());
        return Results.failure(BaseErrorCode.CLIENT_ERROR.code(),
                ex.getMessage() == null ? "账号异常" : ex.getMessage());
    }

    /**
     * 数据库唯一索引冲突(phone 唯一、并发注册撞 username)
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicate(DuplicateKeyException ex) {
        log.warn("唯一索引冲突:{}", ex.getMessage());
        return Results.failure(BaseErrorCode.USER_NAME_EXIST_ERROR.code(), "用户名或手机号已存在");
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception ex) {
        log.error("系统异常", ex);
        return Results.failure();
    }
}
