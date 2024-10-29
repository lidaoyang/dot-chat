package com.dot.comm.em;

/**
 * 用户类型枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2023/6/1 15:25
 */
public enum UserTypeEm {
    ENTERPRISE("ENTERPRISE", "服务商"),
    SUPPLIER("SUPPLIER", "供应商"),
    PL_ADMIN("PL_ADMIN", "平台管理员"),
    ENT_USER("ENT_USER", "企业用户");

    private UserTypeEm(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static UserTypeEm getByCode(String code) {
        for (UserTypeEm em : UserTypeEm.values()) {
            if (em.getCode().equals(code)) {
                return em;
            }
        }
        return null;
    }

    public static String getDescByCode(String code) {
        for (UserTypeEm em : UserTypeEm.values()) {
            if (em.getCode().equals(code)) {
                return em.getDesc();
            }
        }
        return null;
    }
}
