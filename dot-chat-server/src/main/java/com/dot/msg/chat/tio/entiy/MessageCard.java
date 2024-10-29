package com.dot.msg.chat.tio.entiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 名片消息对象(个人名片/群名片)
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:40
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageCard implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称/群名称
     */
    private String nickname;

    /**
     * 用户头像/群头像
     */
    private String avatar;

    /**
     * 用户ID/群ID
     */
    private String userId;

}
