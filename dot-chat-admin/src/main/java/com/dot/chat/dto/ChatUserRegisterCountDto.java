package com.dot.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室用户注册统计对象
 *
 * @author Dao-yang
 * @date: 2025-03-16 09:44:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatUserRegisterCountDto", description = "聊天室用户注册统计对象")
public class ChatUserRegisterCountDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 4652645602672005928L;

    /**
     * 注册日期
     */
    @Schema(description = "注册日期")
    private String date;

    /**
     * 注册人数
     */
    @Schema(description = "注册人数")
    private int num;


}
