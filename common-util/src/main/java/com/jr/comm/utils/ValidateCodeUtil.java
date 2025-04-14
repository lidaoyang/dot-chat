package com.jr.comm.utils;

import cn.hutool.core.util.ReUtil;
import com.jr.comm.constants.RegularConstants;
import com.jr.comm.constants.SmsConstants;
import com.jr.comm.em.ExceptionCodeEm;
import com.jr.comm.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 验证码验证工具类
 *
 * @author: Dao-yang.
 * @date: Created in 2024/11/13 17:51
 */
@Slf4j
public class ValidateCodeUtil {

    private final RedisUtil redisUtil;

    private ValidateCodeUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public static ValidateCodeUtil getInstance(RedisUtil redisUtil) {
        return new ValidateCodeUtil(redisUtil);
    }

    /**
     * 检测手机验证码
     *
     * @param phone 手机号
     * @param code  验证码
     */
    public void checkValidateCode(String phone, String code) {
        boolean matchPhone = ReUtil.isMatch(RegularConstants.PHONE, phone);
        if (!matchPhone) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "手机号格式错误，请输入正确得手机号");
        }
        String validateCode = redisUtil.get(SmsConstants.SMS_VALIDATE_PHONE + phone);
        if (StringUtils.isBlank(validateCode)) {
            log.error("验证码已过期,手机号:{},验证码:{}", phone, code);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "验证码已过期,请重新发送验证码");
        }
        String key = SmsConstants.SMS_VALIDATE_PHONE + "err:" + phone;
        if (!validateCode.equals(code)) {
            // 检查验证码错误次数
            checkValidateCodeErrCount(phone, key);
            log.error("验证码错误,手机号:{},验证码:{},validateCode:{}", phone, code, validateCode);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "验证码错误");
        }

        // 如果号码在白名单中不执行删除验证码
        if (isInWhiteList(phone)) {
            return;
        }
        // 删除缓存错误次数
        redisUtil.remove(key);
        // 删除验证码
        redisUtil.remove(SmsConstants.SMS_VALIDATE_PHONE + phone);
        // 删除验证码过期时间
        redisUtil.remove(SmsConstants.SMS_VALIDATE_PHONE + "timer:" + phone);
    }

    private boolean isInWhiteList(String phone) {
        if (redisUtil.exists(SmsConstants.SMS_VALIDATE_PHONE_WHITE_LIST)) {
            Set<String> ret = redisUtil.setMembers(SmsConstants.SMS_VALIDATE_PHONE_WHITE_LIST);
            for (String v : ret) {
                if (v.equals(phone)) {
                    log.warn("手机号:{},在白名单中,不删除验证码", phone);
                    return true;
                }
            }
        }
        return false;
    }

    private void checkValidateCodeErrCount(String phone, String key) {
        // 获取短信验证码过期时间
        long codeExpire = 5;
        if (redisUtil.exists(key)) {
            int errCount = Integer.parseInt(redisUtil.get(key)) + 1;
            if (errCount >= 5) {
                // 错误5次后删除缓存验证码
                redisUtil.remove(SmsConstants.SMS_VALIDATE_PHONE + phone);
                redisUtil.remove(key);
            } else {
                redisUtil.set(key, String.valueOf(errCount), codeExpire, TimeUnit.MINUTES);
            }
        } else {
            redisUtil.set(key, "1", codeExpire, TimeUnit.MINUTES);
        }
    }

}
