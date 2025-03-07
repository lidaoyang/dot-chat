package com.dot.sys.auth.em;

import lombok.Getter;

/**
 * @author: Dao-yang.
 * @date: Created in 2023/3/22 12:26
 */
@Getter
public enum RoleTypeEm {

    SUPER_ADMIN(0, "超级管理员"),
    GENERAL_ADMIN(1, "普通管理员"),
    DEMO_ADMIN(2, "演示管理员"),
    ;

    RoleTypeEm(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;

    private final String desc;

    public static RoleTypeEm getByCode(int code) {
        for (RoleTypeEm em : RoleTypeEm.values()) {
            if (em.getCode() == code) {
                return em;
            }
        }
        return null;
    }

    public static String getDescByCode(int code) {
        for (RoleTypeEm em : RoleTypeEm.values()) {
            if (em.getCode() == code) {
                return em.getDesc();
            }
        }
        return "unknown";
    }
}
