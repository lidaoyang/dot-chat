package com.jr.comm.exception;

import cn.hutool.core.collection.CollUtil;
import com.jr.comm.em.ExceptionCodeEm;
import com.jr.comm.entity.ResultBean;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;
import java.util.Set;

/**
 * 全局参数、异常拦截
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截表单参数校验
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({BindException.class})
    public ResultBean<Object> bindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
        log.error("参数检验异常,msg:{}", message);
        return ResultBean.failed(ExceptionCodeEm.VALIDATE_FAILED, message);
    }

    /**
     * 拦截JSON参数校验
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultBean<Object> bindException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
        log.error("对象参数校验异常,msg:{}", message);
        return ResultBean.failed(ExceptionCodeEm.VALIDATE_FAILED, message);
    }

    /**
     * 拦截参数类型不正确
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResultBean<Object> bindException(HttpMediaTypeNotSupportedException e) {
        log.error("参数类型不正确", e);
        return ResultBean.failed(ExceptionCodeEm.PRAM_NOT_MATCH, e);
    }

    /**
     * 不支持的请求方法
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResultBean<Object> notSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("不支持的请求方法", e);
        return ResultBean.failed(ExceptionCodeEm.FORBIDDEN, e);
    }

    /**
     * 。当在验证过程中发生约束违规时，该异常被抛出
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({UnexpectedTypeException.class})
    public ResultBean<Object> unexpectedTypeException(UnexpectedTypeException e) {
        log.error("参数校验时发生约束违规", e);
        return ResultBean.failed(ExceptionCodeEm.SYSTEM_ERROR, "请求参数校验异常");
    }

    /**
     * 找不到地址404
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResultBean<Object> bindException(NoResourceFoundException e) {
        log.error("接口地址错误,msg:{}", e.getMessage(), e);
        return ResultBean.failed(ExceptionCodeEm.NOT_FOUND, e.getMessage());
    }

    /**
     * 上传文件超过最大限制
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ResultBean<Object> maxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("上传文件超过最大限制", e);
        return ResultBean.failed(ExceptionCodeEm.VALIDATE_FAILED, "上传文件超过最大请求上限300MB");
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<String> handleAsyncRequestError(AsyncRequestNotUsableException e) {
        return ResponseEntity.status(503).body("SSE Connection lost. Please reconnect.");
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<String> handleAsyncRequestError(AsyncRequestTimeoutException e) {
        return ResponseEntity.status(503).body("SSE Connection timeout. Please reconnect.");
    }

    // 声明要捕获的异常
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public <T> ResultBean<?> defaultExceptionHandler(Exception e) {
        String message = e.getMessage();
        if (e instanceof ApiException apiE) {
            log.error("服务异常", apiE);
            return ResultBean.failed(apiE);
        }

        if (e instanceof MissingServletRequestParameterException) {
            log.error("必传参数为空", e);
            return ResultBean.failed(ExceptionCodeEm.PRAM_NOT_MATCH, message);
        }
        if (e instanceof ConstraintViolationException exception) {
            Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
            if (CollUtil.isNotEmpty(constraintViolations)) {
                for (ConstraintViolation<?> cv : constraintViolations) {
                    log.error("参数校验失败,msg:{}", cv.getMessage());
                    return ResultBean.failed(ExceptionCodeEm.PRAM_NOT_MATCH, cv.getMessage());
                }
            }
            log.error("参数校验失败", e);
            return ResultBean.failed(ExceptionCodeEm.PRAM_NOT_MATCH, message);
        }

        if ("org.apache.dubbo.rpc.RpcException".equals(e.getClass().getName())) {
            message = message.replace("java.util.concurrent.ExecutionException: org.apache.dubbo.rpc.RpcException: ", "");
            log.error("RPC服务异常", e);
            if (message.contains("No provider available")) {
                return ResultBean.failed(ExceptionCodeEm.SYSTEM_ERROR, "依赖的服务未启动");
            }
            return ResultBean.failed(ExceptionCodeEm.SYSTEM_ERROR, message);
        }
        log.error("未知异常", e);
        return ResultBean.failed(ExceptionCodeEm.SYSTEM_ERROR);
    }
}
