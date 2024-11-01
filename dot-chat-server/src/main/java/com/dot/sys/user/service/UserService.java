package com.dot.sys.user.service;

import com.dot.sys.user.request.LoginRequest;
import com.dot.sys.user.response.LoginResponse;

import java.util.List;

/**
 * 用户表服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
public interface UserService {

    /**
     * 账号密码登录
     *
     * @return LoginResponse
     */
    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshToken();

    void logout();

    /**
     * 更新密码
     *
     * @param oldPwd   旧密码
     * @param newPwd   新密码
     * @return 更新结果
     */
    Boolean updatePassword(String oldPwd, String newPwd);

    /**
     * 注册并登录
     *
     * @param phone    手机号
     * @param password 密码
     * @return 注册结果
     */
    LoginResponse registerAndLogin(String phone, String password, String nickname);
}
