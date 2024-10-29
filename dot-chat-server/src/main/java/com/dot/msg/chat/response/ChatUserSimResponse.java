package com.dot.msg.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户信息(精简)
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserSimResponse", description = "聊天室用户信息(精简)")
public class ChatUserSimResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 9191778674935372365L;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Integer userId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;

    /**
     * 是否好友
     */
    @Schema(description = "是否好友")
    private Boolean isFriend;

    /**
     * 是否解散
     */
    @Schema(description = "是否解散")
    private Boolean isDissolve;
}
