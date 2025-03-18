package com.dot.sys.auth.service;


import com.dot.sys.auth.request.LoginRequest;
import com.dot.sys.auth.response.LoginResponse;

/**
 * 用户表服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
public interface LoginService {

    /**
     * 账号密码登录
     *
     * @return LoginResponse
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 刷新token
     *
     * @return LoginResponse
     */
    LoginResponse refreshToken();

    /**
     * 退出登录
     */
    void logout();

}
