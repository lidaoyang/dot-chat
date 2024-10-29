package com.dot.sys.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.comm.em.UserTypeEm;
import com.dot.sys.user.model.User;
import com.dot.sys.user.request.LoginRequest;
import com.dot.sys.user.response.LoginResponse;

import java.util.List;

/**
 * 用户表服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
public interface UserService extends IService<User> {

    /**
     * 账号密码登录
     *
     * @return LoginResponse
     */
    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshToken(UserTypeEm userType);

    void logout(UserTypeEm userType);

    /**
     * 根据手机号查询用户
     *
     * @param enterpriseId 企业ID
     * @param phone        用户手机号
     * @return 用户信息
     */
    User getUser(Integer enterpriseId, String phone);

    List<Integer> getEnterpriseIdList(String account);

    /**
     * 更新密码
     *
     * @param userType 用户类型
     * @param oldPwd   旧密码
     * @param newPwd   新密码
     * @return 更新结果
     */
    Boolean updatePassword(UserTypeEm userType, String oldPwd, String newPwd);

    /**
     * 注册并登录
     *
     * @param phone    手机号
     * @param password 密码
     * @return 注册结果
     */
    LoginResponse registerAndLogin(String phone, String password, String nickname);
}
