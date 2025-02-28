package com.dot.comm.utils;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * 公共工具类
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/11 12:39
 */
public class CommUtil {


    public static String encryptPassword(String pwdPlaintext, String key) {
        try {
            // Security.addProvider(new SunJCE());
            // Security.getProvider("SunJCE");
            Key _key = getDESSercretKey(key);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, _key);
            byte[] data = pwdPlaintext.getBytes(StandardCharsets.UTF_8);
            byte[] result = cipher.doFinal(data);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("密码处理异常");
        }
    }

    /**
     * 解密密码
     */
    public static String decryptPassowrd(String pwdCiphertext, String key) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        Key aKey = getDESSercretKey(key);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, aKey);

        byte[] data = Base64.getDecoder().decode(pwdCiphertext);
        byte[] result = cipher.doFinal(data);

        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * 获得DES加密秘钥
     *
     * @param key key
     * @return s
     */
    public static SecretKey getDESSercretKey(String key) {
        byte[] result = new byte[8];
        byte[] keys;
        keys = key.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < 8; i++) {
            if (i < keys.length) {
                result[i] = keys[i];
            } else {
                result[i] = 0x01;
            }
        }
        return new SecretKeySpec(result, "DES");
    }

    public static String getUlid() {
        Ulid ulid = UlidCreator.getMonotonicUlid();
        return ulid.toString();
    }

    /**
     * 获取fin_in_set拼装sql
     *
     * @param field String 字段
     * @param value Integer 值
     * @return String
     * @author Mr.Zhang
     * @since 2020-04-22
     */
    public static String getFindInSetSql(String field, Integer value) {
        return "find_in_set(" + value + ", " + field + ")";
    }

    /**
     * 获取fin_in_set拼装sql
     *
     * @param field String 字段
     * @param list  ArrayList<Integer> 值
     * @return String
     * @author Mr.Zhang
     * @since 2020-04-22
     */
    public static String getFindInSetSql(String field, List<Integer> list) {
        ArrayList<String> sqlList = new ArrayList<>();
        for (Integer value : list) {
            sqlList.add(getFindInSetSql(field, value));
        }
        return "( " + StringUtils.join(sqlList, " or ") + ")";
    }

    /**
     * 字符串分割，转化为数组
     *
     * @param str 字符串
     * @return List<String>
     */
    public static List<String> stringToArrayStr(String str) {
        return stringToArrayStrRegex(str, ",");
    }

    /**
     * 数字字符数据转int格式数据
     *
     * @param str 待转换的数字字符串
     * @return int数组
     */
    public static List<Integer> stringToArrayInt(String str) {
        List<String> strings = stringToArrayStrRegex(str, ",");
        List<Integer> ids = new ArrayList<>();
        for (String string : strings) {
            ids.add(Integer.parseInt(string.trim()));
        }
        return ids;
    }

    /**
     * 字符串分割，转化为数组
     *
     * @param str   字符串
     * @param regex 分隔符有
     * @return List<String>
     */
    public static List<String> stringToArrayStrRegex(String str, String regex) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isEmpty(str)) {
            return list;
        }
        if (str.contains(regex)) {
            String[] split = str.split(regex);

            for (String value : split) {
                if (!StringUtils.isBlank(value)) {
                    list.add(value);
                }
            }
        } else {
            list.add(str);
        }
        return list;
    }


    /**
     * hash 转换
     *
     * @param base64 String 图片流
     * @return String
     */
    public static String getBase64Image(String base64) {
        return "data:image/png;base64," + base64;
    }


    public static String getClientIp() {
        HttpServletRequest request = RequestUtil.getRequest();
        return getClientIp(Objects.requireNonNull(request));
    }

    /**
     * 获取客户端ip
     *
     * @param request 参数
     * @return String
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (checkIsIp(ip)) {
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (checkIsIp(ip)) {
            return ip;
        }

        ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            // 本地 localhost访问 ipv6
            ip = "127.0.0.1";
        }
        if (checkIsIp(ip)) {
            return ip;
        }

        return "";
    }

    /**
     * 检测是否为ip
     *
     * @param ip 参数
     * @return String
     */
    public static boolean checkIsIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }

        if ("unKnown".equals(ip)) {
            return false;
        }

        if ("unknown".equals(ip)) {
            return false;
        }

        return ip.split("\\.").length == 4;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            // 单调排序
            System.out.println(UlidCreator.getMonotonicUlid());
        }
        for (int i = 0; i < 10; i++) {
            // 常规
            System.out.println(UlidCreator.getUlid());
        }
        for (int i = 0; i < 10; i++) {
            System.out.println(Ulid.fast().toString());
        }
    }
}
