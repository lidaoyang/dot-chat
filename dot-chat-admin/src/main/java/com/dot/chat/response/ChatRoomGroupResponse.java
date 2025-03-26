package com.dot.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户聊天室群聊返回对象
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatRoomGroupResponse", description = "用户聊天室群聊返回对象")
public class ChatRoomGroupResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 3548666185715971218L;

    /**
     * 聊天室id uid1_uid2
     */
    @Schema(description = "聊天室id")
    private String chatId;

    /**
     * 最新消息发送时间
     */
    @Schema(description = "最新消息发送时间")
    private String lastTime;

    @Schema(description = "聊天室群组信息")
    private ChatGroupResponse group;

    @Schema(description = "聊天室消息列表,")
    private List<ChatMsgUserResponse> msgList;

}
