package com.dot.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户返回对象-精简
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserSimResponse", description = "聊天室用户返回对象-精简")
public class ChatUserSimResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 4652645602672005928L;

    @Schema(description = "id")
    private Integer id;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 用户电话(登录唯一账号)
     */
    @Schema(description = "用户电话(登录唯一账号)")
    private String phone;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;


}
