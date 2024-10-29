package com.dot.msg.chat.em;

/**
 * 聊天室类型枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:43
 */
public enum ChatTypeEm {

    SINGLE("单聊"),
    GROUP("群聊");


    private String desc;

    ChatTypeEm(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static ChatTypeEm getMstType(String name) {
        for (ChatTypeEm mstType : ChatTypeEm.values()) {
            if (mstType.name().equals(name)) {
                return mstType;
            }
        }
        return null;
    }

    public static String getMstTypeDesc(String type) {
        for (ChatTypeEm mstType : ChatTypeEm.values()) {
            if (mstType.getDesc().equals(type)) {
                return mstType.getDesc();
            }
        }
        return null;
    }
}
