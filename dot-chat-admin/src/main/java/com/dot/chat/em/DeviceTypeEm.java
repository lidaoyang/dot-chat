package com.dot.chat.em;

/**
 * 设备类型枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:43
 */
public enum DeviceTypeEm {

    PC("PC端"),
    MOBILE("移动端"),
    SYS("系统消息");


    private String desc;

    DeviceTypeEm(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static DeviceTypeEm getDeviceType(String name) {
        for (DeviceTypeEm typeEm : DeviceTypeEm.values()) {
            if (typeEm.name().equals(name)) {
                return typeEm;
            }
        }
        return null;
    }

    public static String getDeviceTypeDesc(String type) {
        for (DeviceTypeEm typeEm : DeviceTypeEm.values()) {
            if (typeEm.getDesc().equals(type)) {
                return typeEm.getDesc();
            }
        }
        return null;
    }
}
