package com.dot.msg.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 添加聊天室消息记录请求对象
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatMsgAddRequest", description = "添加聊天室消息记录请求对象")
public class ChatMsgAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1725781086462862292L;

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
     * 群组ID,群聊时groupId有值
     */
    @Schema(description = "群组ID,群聊时groupId有值")
    private Integer groupId;

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
     * 发送用户id
     */
    @Schema(description = "发送用户id")
    private Integer sendUserId;

    /**
     * 消息接收用户ID(群消息时为群ID)
     */
    @Schema(description = "消息接收用户ID(群消息时为群ID)")
    private Integer toUserId;

}
