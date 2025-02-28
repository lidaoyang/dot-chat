package com.dot.msg.chat.model;

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
 * 聊天室和用户关联表实体
 * 
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@TableName("chat_room_user_rel")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatRoomUserRel", description="聊天室和用户关联表")
public class ChatRoomUserRel implements Serializable {

	@Serial
	private static final long serialVersionUID =  1163480997563153986L;

	@Schema(description = "id")
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 聊天室id
	 */
	@Schema(description = "聊天室id")
	private String chatId;

	/**
	 * 用户id
	 */
	@Schema(description = "用户id")
	private Integer userId;

	/**
	 * 是否置顶(true:置顶;false:不置顶)
	 */
	@Schema(description = "是否置顶(true:置顶;false:不置顶)")
	private Boolean isTop;

	/**
	 * 消息免打扰(true:开启免打扰;false:关闭免打扰)
	 */
	@Schema(description = "消息免打扰(true:开启免打扰;false:关闭免打扰)")
	private Boolean msgNoDisturb;

	/**
	 * 未读消息数
	 */
	@Schema(description = "未读消息数")
	private Integer unreadMsgCount;

	/**
	 * 离线消息数(离线时,未收消息)
	 */
	@Schema(description = "离线消息数(离线时,未收消息)")
	private Integer offlineMsgCount;

	/**
	 * 最新一条消息id
	 */
	@Schema(description = "最新一条消息id")
	private Integer lastMsgId;

	/**
	 * 最新一条信息(LatestMsg),如果消息类型是图片,显示(“图片”),商品显示“商品”
	 */
	@Schema(description = "最新一条信息(LatestMsg),如果消息类型是图片,显示(“图片”),商品显示“商品”")
	private String lastMsg;

	/**
	 * 最后消息发送时间
	 */
	@Schema(description = "最后消息发送时间")
	private String lastTime;

	/**
	 * 时间戳
	 */
	@Schema(description = "时间戳")
	private Long timestamp;

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
