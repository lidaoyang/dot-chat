package com.dot.msg.chat.tio.em;

/**
 * 通话消息类型枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:43
 */
public enum CallTypeEm {
    invite("通话邀请", "邀请通话"),
    accept("接受通话","接受通话"),
    offer("邀请信令", "邀请信令"),
    answer("应答信令","应答信令"),
    candidate1("候选者1", "候选者1"),
    candidate2("候选者2", "候选者2"),
    cancel("已取消", "对方已取消"),// 取消通话
    refuse("对方已拒绝", "已拒绝"),// 拒绝通话
    hangup("通话时长", "通话时长"),//通话时长 00:11 挂断通话
    no_answer("对方无应答", "未应答"),// 无人接听
    busying("对方正忙", "忙线未接听"),// 忙线未接听
    dropped("通话中断", "通话中断"), //一个人异常下线通话中断
    ;


    private String desc;

    private String desc2;

    CallTypeEm(String desc, String desc2) {
        this.desc = desc;
        this.desc2 = desc2;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }

    public static CallTypeEm getByNme(String name) {
        for (CallTypeEm typeEm : CallTypeEm.values()) {
            if (typeEm.name().equals(name)) {
                return typeEm;
            }
        }
        return null;
    }

    public static String getDescByName(String name) {
        for (CallTypeEm mstType : CallTypeEm.values()) {
            if (mstType.name().equals(name)) {
                return mstType.getDesc();
            }
        }
        return null;
    }

    public static String getDesc2ByName(String name) {
        for (CallTypeEm mstType : CallTypeEm.values()) {
            if (mstType.name().equals(name)) {
                return mstType.getDesc2();
            }
        }
        return null;
    }
}
