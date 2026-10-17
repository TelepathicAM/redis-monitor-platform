package com.danuvius.platform.common.exception;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? BusinessCode.PARAMS_ERROR.getMsg() : fieldError.getDefaultMessage();
        return ResultUtils.error(BusinessCode.PARAMS_ERROR, msg);
    }

    @ExceptionHandler(BindException.class)
    public BaseResponse<?> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? BusinessCode.PARAMS_ERROR.getMsg() : fieldError.getDefaultMessage();
        return ResultUtils.error(BusinessCode.PARAMS_ERROR, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        String msg = BusinessCode.PARAMS_ERROR.getMsg();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            msg = violation.getMessage();
            break;
        }
        return ResultUtils.error(BusinessCode.PARAMS_ERROR, msg);
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e) {
        log.error("系统异常", e);
        return ResultUtils.error(BusinessCode.SYSTEM_ERROR);
    }
}