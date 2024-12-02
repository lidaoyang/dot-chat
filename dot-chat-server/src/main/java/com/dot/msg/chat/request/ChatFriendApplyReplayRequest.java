package com.dot.msg.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室新回复好友申请请求对象
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatFriendApplyReplayRequest", description = "聊天室新回复好友申请请求对象")
public class ChatFriendApplyReplayRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 260395165663474068L;

    /**
     * 申请ID
     */
    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请ID不能为空")
    private Integer applyId;

    /**
     * 回复内容
     */
    @Schema(description = "回复内容")
    private String replayContent;

}
