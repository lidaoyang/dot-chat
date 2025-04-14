package com.dot.comm.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serial;
import java.io.Serializable;

/**
 * 公共返回对象
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultBean<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 905784513600880932L;

    /**
     * 响应编码
     */
    @Schema(description = "响应编码", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private int code;

    /**
     * 成功标志
     */
    @Schema(description = "成功标志", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;

    /**
     * 响应信息
     */
    @Schema(description = "响应信息", example = "操作成功", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    /**
     * 响应信息详情
     */
    @Schema(description = "响应信息详情", example = "操作成功", requiredMode = Schema.RequiredMode.AUTO)
    private String detailMessage;

    /**
     * 请求链路唯一ID
     */
    @Schema(description = "请求链路唯一ID", example = "5119fe60d0b24b77a56b225e895210ac", requiredMode = Schema.RequiredMode.REQUIRED)
    private String traceId = MDC.get(CommConstant.TRACE_ID) == null ? null : MDC.get(CommConstant.TRACE_ID).toString();

    /**
     * 响应数据
     */
    @Schema(description = "响应数据", requiredMode = Schema.RequiredMode.AUTO)
    private T data;

    private ResultBean(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        if (code == ExceptionCodeEm.SUCCESS.getCode()) {
            success = true;
        }
    }

    private ResultBean(int code, String message, String detailMessage, T data) {
        this.code = code;
        this.message = message;
        this.detailMessage = detailMessage;
        this.data = data;
        if (code == ExceptionCodeEm.SUCCESS.getCode()) {
            success = true;
        }
    }

    /**
     * 成功返回结果
     */
    public static <T> ResultBean<T> success() {
        return new ResultBean<T>(ExceptionCodeEm.SUCCESS.getCode(), ExceptionCodeEm.SUCCESS.getMessage(), null);
    }

    /**
     * 成功返回结果
     */
    public static <T> ResultBean<T> success(String message) {
        return new ResultBean<T>(ExceptionCodeEm.SUCCESS.getCode(), message, null);
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> ResultBean<T> success(T data) {
        return new ResultBean<T>(ExceptionCodeEm.SUCCESS.getCode(), ExceptionCodeEm.SUCCESS.getMessage(), data);
    }


    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     * @param message 提示信息
     */
    public static <T> ResultBean<T> success(T data, String message) {
        return new ResultBean<T>(ExceptionCodeEm.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 失败返回结果
     */
    public static <T> ResultBean<T> failed(ExceptionCodeEm errorCode) {
        return new ResultBean<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 失败返回结果
     */
    public static <T> ResultBean<T> failed(ExceptionCodeEm errorCode, T data) {
        return new ResultBean<T>(errorCode.getCode(), errorCode.getMessage(), data);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static <T> ResultBean<T> failed(ExceptionCodeEm errorCode, String message) {
        return new ResultBean<T>(errorCode.getCode(), message, null);
    }

    /**
     * 失败返回结果
     *
     * @param exception 错误异常
     */
    public static <T> ResultBean<T> failed(ApiException exception) {
        return new ResultBean<T>(exception.getErrCode(), exception.getErrMsg(), null);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static <T> ResultBean<T> failed(int errorCode, String message) {
        return new ResultBean<T>(errorCode, message, null);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static <T> ResultBean<T> failed(int errorCode, String message, String detailMessage) {
        return new ResultBean<T>(errorCode, message, detailMessage, null);
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> ResultBean<T> failed(String message) {
        return new ResultBean<T>(ExceptionCodeEm.SYSTEM_ERROR.getCode(), message, null);
    }

    /**
     * 失败返回结果
     *
     * @param data 获取的数据
     * @param message 提示信息
     */
    public static <T> ResultBean<T> failed(T data, String message) {
        return new ResultBean<T>(ExceptionCodeEm.SYSTEM_ERROR.getCode(), message, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> ResultBean<T> failed() {
        return failed(ExceptionCodeEm.SYSTEM_ERROR);
    }

    /**
     * 返回操作结果
     *
     * @param ret 操作结果
     */
    public static ResultBean<Boolean> result(Boolean ret) {
        return ret ? success(true) : failed("操作失败");
    }

    /**
     * 返回操作结果
     *
     * @param ret 操作结果
     */
    public static <T> ResultBean<T> result(Boolean ret, String message) {
        return ret ? success(message) : failed(message);
    }

    /**
     * 返回操作结果
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static <T> ResultBean<T> result(int errorCode, String message, T data) {
        return new ResultBean<T>(errorCode, message, data);
    }

    /**
     * 返回操作结果
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static <T> ResultBean<T> result(int errorCode, String message) {
        return new ResultBean<T>(errorCode, message, null);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> ResultBean<T> validateFailed() {
        return failed(ExceptionCodeEm.VALIDATE_FAILED);
    }

    /**
     * 参数验证失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> ResultBean<T> validateFailed(String message) {
        return new ResultBean<T>(ExceptionCodeEm.VALIDATE_FAILED.getCode(), message, null);
    }

    /**
     * 未登录返回结果
     */
    public static <T> ResultBean<T> unauthorized(T data) {
        return new ResultBean<T>(ExceptionCodeEm.UNAUTHORIZED.getCode(), ExceptionCodeEm.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 未登录返回结果
     */
    public static <T> ResultBean<T> unauthorized() {
        return new ResultBean<T>(ExceptionCodeEm.UNAUTHORIZED.getCode(), ExceptionCodeEm.UNAUTHORIZED.getMessage(), null);
    }

    /**
     * 没有权限查看
     */
    public static <T> ResultBean<T> forbidden() {
        return new ResultBean<T>(ExceptionCodeEm.FORBIDDEN.getCode(), ExceptionCodeEm.FORBIDDEN.getMessage(), null);
    }

    /**
     * 未授权返回结果
     */
    public static <T> ResultBean<T> forbidden(T data) {
        return new ResultBean<T>(ExceptionCodeEm.FORBIDDEN.getCode(), ExceptionCodeEm.FORBIDDEN.getMessage(), data);
    }
}
