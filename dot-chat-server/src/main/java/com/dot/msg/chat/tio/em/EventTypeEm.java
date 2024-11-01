package com.dot.msg.chat.tio.em;

/**
 * 消息事件类型枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:43
 */
public enum EventTypeEm {
    FRIEND_APPLY("好友申请"),
    FRIEND_REJECT("加好友被拒绝"),
    FRIEND_AGREE("加好友被同意"),
    GROUP_CREATE("创建群聊"),
    GROUP_APPLY("入群申请"),
    GROUP_REJECT("入群被拒绝"),
    GROUP_AGREE("入群被同意"),
    GROUP_JOIN("加入群聊"),
    GROUP_KICK("被踢出群"),
    GROUP_EXIT("退出群"),
    GROUP_DISMISS("群解散"),
    GROUP_BAN("群禁言"),
    REVOKE_MSG("撤销消息"),
    BIZ_ORDER("订单通知"),
    ;


    private String desc;

    EventTypeEm(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static EventTypeEm getEventType(String name) {
        for (EventTypeEm eventTypeEm : EventTypeEm.values()) {
            if (eventTypeEm.name().equals(name)) {
                return eventTypeEm;
            }
        }
        return null;
    }

    public static String getMstTypeDesc(String type) {
        for (EventTypeEm mstType : EventTypeEm.values()) {
            if (mstType.getDesc().equals(type)) {
                return mstType.getDesc();
            }
        }
        return null;
    }
}
