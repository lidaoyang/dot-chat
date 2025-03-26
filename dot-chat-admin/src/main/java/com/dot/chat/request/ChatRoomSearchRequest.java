package com.dot.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室搜索请求参数
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserSearchRequest", description = "聊天室搜索请求参数")
public class ChatRoomSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 4652645602672005928L;

    /**
     * 开始日期
     */
    @Schema(description = "参与用户ID")
    private Integer userId;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期")
    private String startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    private String entDate;


}
