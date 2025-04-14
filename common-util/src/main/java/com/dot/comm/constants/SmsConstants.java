package com.dot.comm.constants;

/**
 * 短信常量类
 */
public final class SmsConstants {

    /**
     * 手机验证码redis key
     */
    public static final String SMS_VALIDATE_PHONE = "sms:validate:code:";

    /**
     * 手机验证码redis key
     */
    public static final String SMS_VALIDATE_PHONE_WHITE_LIST = "sms:validate:code:white_list";

    /**
     * 验证码过期时间
     */
    public static final int CODE_EXPIRE = 5;
}
