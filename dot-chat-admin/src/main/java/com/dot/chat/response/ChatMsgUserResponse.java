package com.dot.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室消息记录表实体
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatMsgUserResponse", description = "聊天室消息记录表")
public class ChatMsgUserResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 6032304632563448233L;

    @Schema(description = "聊天室用户")
    private ChatUserSimResponse user;

    /**
     * 消息id
     */
    @Schema(description = "消息id")
    private Integer id;

    /**
     * 会话id
     */
    @Schema(description = "会话id")
    private String chatId;

    /**
     * 消息类型(TEXT:文本;PRODUCT:商品;IMAGE:图片;VIDEO:视频;FILE:文件;BIZ_CARD:个人名片;GROUP_BIZ_CARD:群名片;)
     */
    @Schema(description = "消息类型(TEXT:文本;PRODUCT:商品;IMAGE:图片;VIDEO:视频;FILE:文件;BIZ_CARD:个人名片;GROUP_BIZ_CARD:群名片;)")
    private String msgType;

    /**
     * 发送用户ID
     */
    @Schema(description = "发送用户ID")
    private Integer sendUserId;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String msg;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private String sendTime;

    /**
     * 设备类型(PC,MOBILE)
     */
    @Schema(description = "设备类型(PC,MOBILE)")
    private String deviceType;

}
