package com.dot.msg.notify.em;

/**
 * 通知业务枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:43
 */
public enum NotifyBizEm {
    BIZ_ENT_ORDER(9000001, "企业订单业务"),// 企业订单业务
    BIZ_SUPP_ORDER(9000002, "供应商订单业务") // 供应商订单业务
    ;


    private int id;

    private String desc;

    NotifyBizEm(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getBizId(Integer entId) {
        return this.getId() + "_" + entId;
    }

    public static Integer getEntId(String bizid) {
        return Integer.parseInt(bizid.substring(bizid.indexOf("_") + 1));
    }

    public static NotifyBizEm get(String bizid) {
        for (NotifyBizEm bizEm : NotifyBizEm.values()) {
            if (bizEm.getId() == Integer.parseInt(bizid.substring(0, bizid.indexOf("_")))) {
                return bizEm;
            }
        }
        return null;
    }

    public static String getDesc(int id, String defaultDesc) {
        for (NotifyBizEm bizEm : NotifyBizEm.values()) {
            if (bizEm.getId() == id) {
                return bizEm.getDesc();
            }
        }
        return defaultDesc;
    }
}
