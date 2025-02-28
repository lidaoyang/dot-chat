package com.dot.comm.exception;


import com.dot.comm.em.ExceptionCodeEm;

/**
 * Api异常类
 */
public class ApiException extends RuntimeException {

    private int errCode;
    private String errMsg;

    public ApiException() {}

    public ApiException(ExceptionCodeEm codeEm) {
        super(codeEm.getMessage());
        this.errCode = codeEm.getCode();
        this.errMsg = codeEm.getMessage();
    }

    public ApiException(ExceptionCodeEm codeEm, String errMsg) {
        super(errMsg);
        this.errCode = codeEm.getCode();
        this.errMsg = errMsg;
    }

    public ApiException(int errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public ApiException(Throwable cause) {
        super(cause);
        this.errMsg = cause.getMessage();
        this.errCode = ExceptionCodeEm.SYSTEM_ERROR.getCode();
    }

    public ApiException(int errCode, Throwable cause) {
        super(cause);
        this.errCode = errCode;
        this.errMsg = cause.getMessage();
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
