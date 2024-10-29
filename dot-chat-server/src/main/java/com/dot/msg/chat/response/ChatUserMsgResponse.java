package com.dot.msg.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户消息记录
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserMsgResponse", description = "聊天室用户消息记录")
public class ChatUserMsgResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1725781086462862292L;

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
     * 聊天室类型(SINGLE:单聊,GROUP:群聊)
     */
    @Schema(description = "聊天室类型(SINGLE:单聊,GROUP:群聊)")
    private String chatType;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Integer userId;

    /**
     * 发送信息的用户ID
     */
    @Schema(description = "发送信息的用户ID")
    private Integer sendUserId;

    /**
     * 发送用户昵称
     */
    @Schema(description = "发送用户昵称")
    private String nickname;

    /**
     * 发送用户头像
     */
    @Schema(description = "发送用户头像")
    private String avatar;

    /**
     * 消息的发送或接收人(群消息时为发送人)
     */
    @Schema(description = "消息的发送或接收人(群消息时为发送人)")
    private Integer toUserId;

    /**
     * 消息类型(TEXT:文本;PRODUCT:商品;IMAGE:图片;VIDEO:视频;FILE:文件;BIZ_CARD:个人名片;GROUP_BIZ_CARD:群名片;)
     */
    @Schema(description = "消息类型(TEXT:文本;PRODUCT:商品;IMAGE:图片;VIDEO:视频;FILE:文件;BIZ_CARD:个人名片;GROUP_BIZ_CARD:群名片;)")
    private String msgType;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String msg;

    /**
     * 是否未读
     */
    @Schema(description = "是否未读")
    private Boolean isUnread;

    /**
     * 是否是离线消息(离线时,未收消息)
     */
    @Schema(description = "是否是离线消息(离线时,未收消息)")
    private Boolean isOffline;

    /**
     * 是否收藏
     */
    @Schema(description = "是否收藏")
    private Boolean isCollect;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private String sendTime;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳")
    private Integer timestamp;

}
