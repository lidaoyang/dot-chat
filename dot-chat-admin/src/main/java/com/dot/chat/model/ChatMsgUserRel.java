package com.dot.chat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天信息和用户关联表实体
 * 
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@TableName("chat_msg_user_rel")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatMsgUserRel", description="聊天信息和用户关联表")
public class ChatMsgUserRel implements Serializable {

	@Serial
	private static final long serialVersionUID =  1479422366339638039L;

	@Schema(description = "id")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 聊天室ID
	 */
	@Schema(description = "聊天室ID")
	private String chatId;

	/**
	 * 用户id
	 */
	@Schema(description = "用户id")
	private Integer userId;

	/**
	 * 聊天信息id
	 */
	@Schema(description = "聊天信息id")
	private Integer msgId;

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
	 * 发送/接收时间戳
	 */
	@Schema(description = "发送/接收时间戳")
	private Integer sendTimeStamp;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private String createTime;

	/**
	 * 更新时间
	 */
	@Schema(description = "更新时间")
	private String updateTime;

}
