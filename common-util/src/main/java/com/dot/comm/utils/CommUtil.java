package com.dot.comm.utils;

import cn.hutool.core.util.URLUtil;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;

/**
 * 公共工具类
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/11 12:39
 */
@Slf4j
public class CommUtil {

    /**
     * 构建url,把参数拼接到url上
     *
     * @param url    url
     * @param params 参数
     * @return String
     */
    public static String buildUrl(String url, Map<String, String> params) {
        String query = URLUtil.buildQuery(params, StandardCharsets.UTF_8);
        return url + "?" + query;
    }

    /**
     * 获取fin_in_set拼装sql
     *
     * @param field String 字段
     * @param value Integer 值
     */
    public static String getFindInSetSql(String field, Integer value) {
        return "find_in_set(" + value + ", " + field + ")";
    }

    /**
     * 获取fin_in_set拼装sql
     *
     * @param field String 字段
     * @param list  ArrayList<Integer> 值
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
     * 根据长度生成随机数字
     *
     * @param start 起始数字
     * @param end   结束数字
     * @return 生成的随机码
     */
    public static Integer randomCount(Integer start, Integer end) {
        return (int) (Math.random() * (end - start + 1) + start);
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

    /**
     * 判断当前是否是工作时间(周一到周五 9:15-15:45)
     *
     * @return boolean
     */
    public static boolean isWorkTime() {
        LocalTime time = LocalTime.now();
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        log.debug("当前时间：{},dayOfWeek:{}", time, dayOfWeek);
        return dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY && // 周一至周五
               time.isAfter(LocalTime.of(9, 15)) && time.isBefore(LocalTime.of(15, 45));// 9:15-15:45之间
    }

    public static String getUlid() {
        Ulid ulid = UlidCreator.getMonotonicUlid();
        return ulid.toString();
    }

    public static void main(String[] args) {

    }
}
