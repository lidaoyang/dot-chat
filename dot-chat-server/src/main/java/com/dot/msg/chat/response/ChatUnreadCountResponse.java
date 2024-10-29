package com.dot.msg.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室未读消息数统计
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUnreadCountResponse", description = "聊天室未读消息数统计")
public class ChatUnreadCountResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1725781086462862292L;

    /**
     * 聊天室未读数
     */
    @Schema(description = "聊天室未读数")
    private long chatUnreadCount;

    /**
     * 好友的未读数
     */
    @Schema(description = "好友的未读数")
    private long friendUnreadCount;


}
