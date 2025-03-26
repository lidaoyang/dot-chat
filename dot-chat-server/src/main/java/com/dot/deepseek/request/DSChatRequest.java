package com.dot.deepseek.request;

import com.dot.deepseek.em.DSMessageRoleEm;
import com.dot.deepseek.entity.DSChatRequestMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek AI 接口请求信息对象
 *
 * @author: Dao-yang.
 * @date: Created in 2025/2/6 10:17
 */
@Data
@Schema(name = "DSChatRequest", description = "DeepSeek AI 请求对象")
public class DSChatRequest {

    /**
     * 会话id
     */
    @Schema(description = "会话id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会话id不能为空")
    private String chatId;

    @Valid
    @Schema(description = "消息对象列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 8, message = "消息对象不能少于1个超过8个")
    private List<DSChatRequestMessage> messages;
}
