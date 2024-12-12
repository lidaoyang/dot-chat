package com.dot.comm.utils;

import cn.hutool.extra.pinyin.PinyinUtil;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;

/**
 * 汉字拼音转换类
 */
public class PinYinUtil {

    /**
     * 获取字符串拼音的第一个字母
     *
     * @param chinese 汉字
     * @return 汉字首字母(小写)
     */
    public static String getFirstLetterLower(String chinese) {
        return getFirstLetter(chinese, HanyuPinyinCaseType.LOWERCASE);
    }

    /**
     * 获取字符串拼音的第一个字母
     *
     * @param chinese 汉字
     * @return 汉字首字母(大写)
     */
    public static String getFirstLetterUpper(String chinese) {
        return getFirstLetter(chinese, HanyuPinyinCaseType.UPPERCASE);
    }

    /**
     * 获取字符串拼音的第一个字母
     *
     * @param chinese  汉字
     * @param caseType 转换类型(大写:UPPERCASE,小写:LOWERCASE)
     * @return 所有汉字首字母
     */
    private static String getFirstLetter(String chinese, HanyuPinyinCaseType caseType) {
        StringBuilder pinyinStr = new StringBuilder();
        char[] newChar = chinese.toCharArray();  // 转为单个字符
        for (char c : newChar) {
            if (c > 128) {
                char firstLetter = PinyinUtil.getFirstLetter(c);
                pinyinStr.append(firstLetter);
            } else {
                pinyinStr.append(c);
            }
        }
        if (caseType == HanyuPinyinCaseType.UPPERCASE) {
            return pinyinStr.toString().toUpperCase();
        }
        return pinyinStr.toString();
    }

    /**
     * 汉字转为拼音
     *
     * @param chinese 汉字
     * @return 汉字全拼
     */
    public static String getPinyin(String chinese) {
        return PinyinUtil.getPinyin(chinese, "");
    }

    public static void main(String[] args) {
        String str = "雨一直下";
        System.out.println(getPinyin(str));
        System.out.println(getFirstLetterUpper(str));
        System.out.println(PinyinUtil.getPinyin(str, ""));
        System.out.println(PinyinUtil.getFirstLetter(str.charAt(0)));
    }
}