package com.dot.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户搜索请求参数
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserSearchRequest", description = "聊天室用户搜索请求参数")
public class ChatUserSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 4652645602672005928L;

    /**
     * 搜索关键词(ID,name,account)
     */
    @Schema(description = "搜索关键词(ID,name,account)")
    private String keywords;

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

}
