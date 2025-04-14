package com.dot.sse.em;

import lombok.Getter;

/**
 * SSE 事件源 key 枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/4/18 10:06
 */
@Getter
public enum EventSourceKeyEm {

    DEEPSEEK("DeepSeekAPI事件"),
    ;
    private final String desc;

    EventSourceKeyEm(String desc) {
        this.desc = desc;
    }

    public static String getDescByName(String name) {
        for (EventSourceKeyEm em : EventSourceKeyEm.values()) {
            if (em.name().equals(name)) {
                return em.getDesc();
            }
        }
        return null;
    }
}
