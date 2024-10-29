package com.dot.comm.utils;

import cn.hutool.core.util.CharUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

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
    public static String toFirstCharLower(String chinese) {
        return toFirstChar(chinese, HanyuPinyinCaseType.LOWERCASE);
    }

    /**
     * 获取字符串拼音的第一个字母
     *
     * @param chinese 汉字
     * @return 汉字首字母(小写)
     */
    public static String toFirstCharUpper(String chinese) {
        return toFirstChar(chinese, HanyuPinyinCaseType.UPPERCASE);
    }

    /**
     * 获取字符串拼音的第一个字母
     *
     * @param chinese  汉字
     * @param caseType 转换类型(大写:UPPERCASE,小写:LOWERCASE)
     * @return 汉字首字母
     */
    public static String toFirstChar(String chinese, HanyuPinyinCaseType caseType) {
        StringBuilder pinyinStr = new StringBuilder();
        char[] newChar = chinese.toCharArray();  //转为单个字符
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(caseType);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char c : newChar) {
            if (c > 128) {
                try {
                    pinyinStr.append(PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)[0].charAt(0));
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                if (caseType == HanyuPinyinCaseType.UPPERCASE && CharUtil.isLetterLower(c)) {
                    c = Character.toUpperCase(c);
                } else if (caseType == HanyuPinyinCaseType.LOWERCASE && CharUtil.isLetterUpper(c)) {
                    c = Character.toLowerCase(c);
                }
                pinyinStr.append(c);
            }
        }
        return pinyinStr.toString();
    }

    /**
     * 汉字转为拼音
     *
     * @param chinese 汉字
     * @return 汉字全拼
     */
    public static String toPinyin(String chinese) {
        StringBuilder pinyinStr = new StringBuilder();
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char c : newChar) {
            if (c > 128) {
                try {
                    pinyinStr.append(PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)[0]);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinStr.append(c);
            }
        }
        return pinyinStr.toString();
    }

    public static void main(String[] args) {
        String str = "a行业.标兵";
        System.out.println(toPinyin(str));
        System.out.println(toFirstCharUpper(str));
    }
}