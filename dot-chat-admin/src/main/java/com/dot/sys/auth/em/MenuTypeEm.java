package com.dot.sys.auth.em;

import lombok.Getter;

/**
 * @author: Dao-yang.
 * @date: Created in 2023/3/22 12:26
 */
@Getter
public enum MenuTypeEm {

    DIRECTORY(0, "目录菜单"),
    API(1, "API菜单"),
    PAGE(2, "页面菜单");

    MenuTypeEm(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;

    private final String desc;

    public static String getDescByCode(int code) {
        for (MenuTypeEm em : MenuTypeEm.values()) {
            if (em.getCode() == code) {
                return em.getDesc();
            }
        }
        return "unknown";
    }
}
