package com.dot.msg.chat.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@Schema(name = "ChatUserSimDto", description = "聊天室用户信息(精简)")
public class ChatUserSimDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 9191778674935372365L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户昵称(包含好友备注)
     */
    @Schema(description = "用户昵称(包含好友备注)")
    private String nickname;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String olnickname;

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
     * 是否在线
     */
    @Schema(description = "是否在线")
    private Boolean isOnline;

}
