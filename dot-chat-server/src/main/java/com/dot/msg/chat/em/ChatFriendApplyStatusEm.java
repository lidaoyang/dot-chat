package com.dot.msg.chat.em;

/**
 * 聊天室类型枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:43
 */
public enum ChatFriendApplyStatusEm {

    APPLYING(0), //申请中
    AGREE(1); //同意

    ChatFriendApplyStatusEm(int status) {
        this.status = status;
    }

    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public static ChatFriendApplyStatusEm getMstType(String name) {
        for (ChatFriendApplyStatusEm mstType : ChatFriendApplyStatusEm.values()) {
            if (mstType.name().equals(name)) {
                return mstType;
            }
        }
        return null;
    }
}