package com.dot.msg.chat.tio.em;

/**
 * 消息类型枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:43
 */
public enum MsgTypeEm {

    //消息类型(TEXT:文本消息;PRODUCT:商品消息;IMAGE:图片消息;VIDEO:视频消息;FILE:文件消息;BIZ_CARD:个人名片消息;GROUP_BIZ_CARD:群名片消息;
    // SYSTEM:系统消息;EVENT:事件消息;WARNING:预警消息;HEART_BEAT:心跳监测消息;NOTICE:通知消息;)
    ALL("全部消息"),//不包含系统消息,预警消息,事件消息
    TEXT("文本"),
    PRODUCT("商品"),
    IMAGE("图片"),
    VIDEO("视频"),
    FILE("文件"),
    BIZ_CARD("个人名片"),
    GROUP_BIZ_CARD("群名片"),
    SYSTEM("系统"),
    SYSTEM_WARNING("系统警告"),
    EVENT("事件"),
    WARNING("预警"),
    HEART_BEAT("心跳监测"),
    NOTICE("通知"),
    VIDEO_CALL("视频通话"),
    AUDIO_CALL("语音通话"),
    ;


    private String desc;

    MsgTypeEm(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static MsgTypeEm getMstType(String name) {
        for (MsgTypeEm mstType : MsgTypeEm.values()) {
            if (mstType.name().equals(name)) {
                return mstType;
            }
        }
        return null;
    }

    public static String getMstTypeDesc(String type) {
        for (MsgTypeEm mstType : MsgTypeEm.values()) {
            if (mstType.name().equals(type)) {
                return mstType.getDesc();
            }
        }
        return null;
    }
}
