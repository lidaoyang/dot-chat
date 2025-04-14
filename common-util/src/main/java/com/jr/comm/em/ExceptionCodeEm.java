package com.jr.comm.em;

/**
 * 公共异常枚举
 */
public enum ExceptionCodeEm {
    SUCCESS(200, "操作成功"),
    PRAM_NOT_MATCH(400, "参数列表错误（缺少或格式不匹配）"),
    VALIDATE_FAILED(400, "参数检验失败（缺少或格式不匹配）"),
    UNAUTHORIZED(401, "未授权，Token无效或Token过期"),
    FORBIDDEN(403, "访问受限，没有相关权限或授权过期"),
    NOT_FOUND(404, "没有找到相关数据"),
    DATA_ALREADY(444, "数据已存在"),

    ACCESS_RESTRICTED(429, "访问已受限制，请稍候重试"),
    SYSTEM_ERROR(500, "系统异常"),
    SYSTEM_UNAVAILABLE(503, "系统服务不可用");

    private int code;

    private String message;

    private ExceptionCodeEm(int code, String message) {
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
