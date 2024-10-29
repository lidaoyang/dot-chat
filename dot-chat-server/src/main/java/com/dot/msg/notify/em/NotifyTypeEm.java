package com.dot.msg.notify.em;

/**
 * 通知类型枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:43
 */
public enum NotifyTypeEm {
    USER(1),//用户通知
    BIZ(2) //业务通知
    ;


    private int type;

    NotifyTypeEm(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static NotifyTypeEm get(String name) {
        for (NotifyTypeEm typeEm : NotifyTypeEm.values()) {
            if (typeEm.name().equals(name)) {
                return typeEm;
            }
        }
        return null;
    }

}
