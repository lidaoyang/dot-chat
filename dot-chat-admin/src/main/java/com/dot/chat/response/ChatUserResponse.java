package com.dot.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户返回对象
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserResponse", description = "聊天室用户返回对象")
public class ChatUserResponse implements Serializable {

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

    /**
     * 性别(0:保密,1:男,2:女)
     */
    @Schema(description = "性别(0:保密,1:男,2:女)")
    private Integer sex;
    private String sexDesc;

    public String getSexDesc() {
        switch (sex) {
            case 0:
                sexDesc = "保密";
                break;
            case 1:
                sexDesc = "男";
                break;
            case 2:
                sexDesc = "女";
                break;
            default:
                sexDesc = "未知";
        }
        return sexDesc;
    }

    /**
     * 用户状态(1:正常,0:禁用)
     */
    @Schema(description = "用户状态(1:正常,0:禁用)")
    private Boolean status;

    /**
     * 是否在线
     */
    @Schema(description = "是否在线")
    private Boolean isOnline;
    private String onlineDesc;

    public String getOnlineDesc() {
        onlineDesc = isOnline ? "在线" : "离线";
        return onlineDesc;
    }

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private String lastLoginTime;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP")
    private String lastIp;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

}
