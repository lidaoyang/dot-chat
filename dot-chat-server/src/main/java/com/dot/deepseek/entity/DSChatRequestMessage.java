package com.dot.deepseek.entity;

import com.dot.deepseek.em.DSMessageRoleEm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DeepSeek AI 接口请求信息对象
 *
 * @author: Dao-yang.
 * @date: Created in 2025/2/6 10:17
 */
@Data
@Schema(name = "DSChatRequestMessage", description="DeepSeek AI 接口请求信息对象")
public class DSChatRequestMessage {

    /**
     * 消息内容
     */
    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 消息角色(system,user,assistant,tool)
     */
    @Schema(description = "消息角色", hidden = true)
    private DSMessageRoleEm role = DSMessageRoleEm.user;

    /**
     * 参与者的名称,可以选填的参与者的名称，为模型提供信息以区分相同角色的参与者。
     */
    @Schema(description = "参与者的名称")
    private String name;
}
