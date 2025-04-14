package com.jr.comm.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: Dao-yang.
 * @date: Created in 2022/7/25 17:55
 */
public class StrUtils {

    /**
     * 清除字符串中的特殊字符
     *
     * @param chars
     * @return
     */
    public static String clearSpecialChars(String chars) {
        if (chars == null) {
            return "";
        }
        String regEx = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， ·、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(chars);
        chars = m.replaceAll("").trim();
        return chars;
    }

    /**
     * 替换字符串中的特殊字符
     *
     * @param chars
     * @return
     */
    public static String replaceSpecialChars(String chars, String repStr) {
        if (chars == null) {
            return "";
        }
        if (repStr == null) {
            repStr = "";
        }
        String regEx = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， ·、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(chars);
        chars = m.replaceAll(repStr).trim();
        return chars;
    }

    public static boolean equals(String str1, String str2) {
        if (StringUtils.isBlank(str1) && StringUtils.isBlank(str2)) {
            return true;
        } else if (StringUtils.isNotBlank(str1) && str1.equals(str2)) {
            return true;
        } else if (StringUtils.isNotBlank(str2) && str2.equals(str1)) {
            return true;
        }
        return false;
    }

    public static String clearRegex(String chars) {
        return clearRegex(chars,"");
    }

    public static String clearRegex(String chars,String repStr) {
        if (chars == null) {
            return "";
        }
        if (repStr == null) {
            repStr = "";
        }
        String regEx = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(chars);
        chars = m.replaceAll(repStr).trim();
        return chars;
    }

    public static String mysqlSpecialCharEscape(String chars) {
        if (StringUtils.isBlank(chars)) {
            return "";
        }
        return chars.trim().replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'").replace("%", "\\%").replace("_", "\\_");
    }

}
