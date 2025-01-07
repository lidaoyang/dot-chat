package com.dot.msg.chat.listener.event;

import com.dot.msg.chat.response.ChatUserResponse;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册后添加默认好友事件
 *
 * @author Dao-yang
 * @date: 2024-02-29 17:11:01
 */

@Data
public class UserRegisterAddDefaultFriendEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 6238525473174356147L;
    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 好友
     */
    private ChatUserResponse friendUser;


    public UserRegisterAddDefaultFriendEvent(Integer userId) {
        friendUser = new ChatUserResponse();
        friendUser.setId(0);
        friendUser.setNickname("体验交互账号");
        this.userId = userId;
    }
}
