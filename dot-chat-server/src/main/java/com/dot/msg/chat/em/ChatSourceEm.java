package com.dot.msg.chat.em;

/**
 * 好友来源枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:43
 */
public enum ChatSourceEm {
    SEARCH("通过搜索添加"),
    CARD("通过名片添加"),
    GROUP("通过群聊添加"),
    QRCODE("通过二维码添加"),
    SYSTEM("系统默认添加");


    private String desc;

    ChatSourceEm(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static ChatSourceEm get(String name) {
        for (ChatSourceEm sourceEm : ChatSourceEm.values()) {
            if (sourceEm.name().equals(name)) {
                return sourceEm;
            }
        }
        return null;
    }

    public static String getDesc(String type) {
        for (ChatSourceEm sourceEm : ChatSourceEm.values()) {
            if (sourceEm.name().equals(type)) {
                return sourceEm.getDesc();
            }
        }
        return null;
    }
}
