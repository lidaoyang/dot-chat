package com.dot.comm.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员用户登录对象
 */
@Slf4j
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginUsername implements Serializable {

    @Serial
    private static final long serialVersionUID = 905784513600880932L;

    /**
     * 账号
     */
    private String account;

    /**
     * 用户ID
     */
    private Integer uid;

    /**
     * 昵称
     */
    private String nickname;


    private static final String SPLIT_CHAR = ",";

    public LoginUsername(Integer uid, String account, String nickname) {
        this.uid = uid;
        this.account = account;
        this.nickname = nickname;
    }

    /**
     * 合并用户名(uid,account,sex,nickname)
     *
     * @return 合并用户名
     */
    public String mergeUsername() {
        return uid + SPLIT_CHAR + account + SPLIT_CHAR + nickname;
    }

    public static LoginUsername splitUsername(String username) {
        String[] us = username.split(SPLIT_CHAR);
        if (us.length < 3) {
            log.error("账号错误,username:{}", username);
            throw new RuntimeException("账号不存在");
        }
        return new LoginUsername(Integer.parseInt(us[0]), us[1], us[2]);
    }

}
