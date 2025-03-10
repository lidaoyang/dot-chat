package com.dot.comm.utils;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * AES 加解密
 *
 * @author: Dao-yang.
 * @date: Created in 2024/10/31 17:22
 */
public class AESUtil {
    /**
     * AES_KEY 加密解密用的密钥Key，可以用26个字母和数字组成的16位，此处使用AES-128-CBC加密模式。
     * AES_VECTOR 向量， 普通aes加密解密需要为16位。
     */
    private static final String AES_KEY = "Es35T";
    private static final String AES_VECTOR = "LX%TB@16!*#WPAZS";

    /**
     * 系统参数密钥的5位后缀参数的key
     */
    private static final String SYS_AES_KEY_5_SUFFIX_KEY = "sys_aes_key_5_suffix";

    public static void main(String[] args) {
        String key = "15158152796";
        String encryptData = encryptCBC(key, "123456");
        System.out.println("CBC加密：" + encryptData);
        String decryptData = decryptCBC(key, encryptData);
        System.out.println("CBC解密：" + decryptData);
        String encryptData2 = encryptECB(key, "123456.");
        System.out.println("ECB加密：" + encryptData2);
        String decryptData2 = decryptECB(key, encryptData2);
        System.out.println("ECB解密：" + decryptData2);
    }

    /**
     * CBC(串行密钥传输)模式加密,CBC是一种比ECB更加安全的加密模式
     *
     * @param aesKeyPrefix AES加密 key前缀,长度11位
     * @param data 待加密数据
     * @return 加密结果
     */
    public static String encryptCBC(String aesKeyPrefix, String data) {
        AES aes = getAes(aesKeyPrefix, Mode.CBC, Padding.PKCS5Padding);
        return aes.encryptBase64(data, StandardCharsets.UTF_8);
    }

    /**
     * CBC(串行密钥传输)模式解密
     *
     * @param aesKeyPrefix AES解密 key前缀,长度11位
     * @param data 待解密数据
     * @return 解密结果
     */
    public static String decryptCBC(String aesKeyPrefix, String data) {
        AES aes = getAes(aesKeyPrefix, Mode.CBC, Padding.PKCS5Padding);
        byte[] decryptDataBase64 = aes.decrypt(data);
        return new String(decryptDataBase64, StandardCharsets.UTF_8);
    }

    /**
     * ECB(电子密码本)模式加密,ECB是最简单的AES加密模式之一
     *
     * @param aesKeyPrefix AES加密 key前缀,长度11位
     * @param data 待加密数据
     * @return 加密结果
     */
    public static String encryptECB(String aesKeyPrefix, String data) {
        AES aes = getAes(aesKeyPrefix, Mode.ECB, Padding.PKCS5Padding);
        return aes.encryptBase64(data, StandardCharsets.UTF_8);
    }

    /**
     * ECB(电子密码本)模式解密
     *
     * @param aesKeyPrefix AES解密 key前缀,长度11位
     * @param data 待解密数据
     * @return 解密结果
     */
    public static String decryptECB(String aesKeyPrefix, String data) {
        AES aes = getAes(aesKeyPrefix, Mode.ECB, Padding.PKCS5Padding);
        byte[] decryptDataBase64 = aes.decrypt(data);
        return new String(decryptDataBase64, StandardCharsets.UTF_8);
    }

    /**
     * 加密
     *
     * @param aesKeyPrefix AES加密 key前缀,长度11位
     * @param data 待加密数据
     * @param mode 加密模式 ECB(电子密码本)模式 CBC(串行密钥传输)模式,CBC是一种比ECB更加安全的加密模式
     * @param padding 填充算法
     * @return 加密结果
     */
    public static String encrypt(String aesKeyPrefix, String data, Mode mode, Padding padding) {
        AES aes = getAes(aesKeyPrefix, mode, padding);
        return aes.encryptBase64(data, StandardCharsets.UTF_8);
    }

    /**
     * 解密
     *
     * @param aesKeyPrefix AES加密 key前缀,长度11位
     * @param data 待解密数据
     * @param mode 加密模式 ECB(电子密码本)模式 CBC(串行密钥传输)模式,CBC是一种比ECB更加安全的加密模式
     * @param padding 填充算法
     * @return 解密结果
     */
    public static String decrypt(String aesKeyPrefix, String data, Mode mode, Padding padding) {
        AES aes = getAes(aesKeyPrefix, mode, padding);
        byte[] decryptDataBase64 = aes.decrypt(data);
        return new String(decryptDataBase64, StandardCharsets.UTF_8);
    }

    /**
     * 获取AES加密解密对象
     *
     * @param aesKeyPrefix AES加密 key前缀,长度11位
     * @param mode 加密模式 ECB(电子密码本)模式 CBC(串行密钥传输)模式,CBC是一种比ECB更加安全的加密模式
     * @param padding 填充算法
     * @return AES对象
     */
    private static AES getAes(String aesKeyPrefix, Mode mode, Padding padding) {
        if (StringUtils.isBlank(aesKeyPrefix) || aesKeyPrefix.length() != 11) {
            throw new RuntimeException("加密 key 前缀不能为空且长度必须为11位");
        }
        // 获取系统参数密钥的后缀参数,系统参数为空时使用默认值
        String aesKeySuffix = System.getProperty(SYS_AES_KEY_5_SUFFIX_KEY, AES_KEY);
        if (aesKeySuffix.length() != 5) {
            throw new RuntimeException("系统参数密钥后缀参数的key长度必须是5位字符串");
        }
        // 密钥 = 前缀(11位) + 后缀(5位)
        String aesKey = aesKeyPrefix + aesKeySuffix;
        if (Mode.CBC == mode) {
            return new AES(mode, padding, new SecretKeySpec(aesKey.getBytes(), "AES"), new IvParameterSpec(AES_VECTOR.getBytes()));
        }
        return new AES(mode, padding, new SecretKeySpec(aesKey.getBytes(), "AES"));
    }

}
