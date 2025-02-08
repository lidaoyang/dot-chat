package com.dot.deepseek.em;

import lombok.Getter;

/**
 * DeepSeek AI 角色枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2025/2/6 10:20
 */
@Getter
public enum DSMessageRoleEm {

    user("用户消息"),//用户消息是由对话的参与者（用户）输入的，表达用户的需求、问题或指令。驱动对话的内容，向系统提出请求或提供信息。
    assistant("助手消息"),//助手消息是由对话系统生成的，用于回应用户的消息，提供答案、建议或执行任务。满足用户的需求，提供有用的信息或执行用户请求的操作。
    system("系统消息"),//系统消息通常由对话系统本身生成，用于设置对话的上下文、规则或提供初始指令。引导对话的流程，定义对话的边界或提供背景信息。
    tool("工具消息");

    private final String role;

    DSMessageRoleEm(String role) {
        this.role = role;
    }

}
