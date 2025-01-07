package com.dot.sys.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.entity.TokenModel;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
import com.dot.comm.utils.AESUtil;
import com.dot.comm.utils.RedisUtil;
import com.dot.msg.chat.model.ChatUser;
import com.dot.msg.chat.service.ChatUserService;
import com.dot.sys.user.request.LoginRequest;
import com.dot.sys.user.response.LoginResponse;
import com.dot.sys.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private ChatUserService chatUserService;

    @Resource
    private TokenManager tokenManager;

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        ChatUser user = chatUserService.getByPhone(loginRequest.getAccount());
        if (ObjectUtil.isNull(user)) {
            log.error("账号不存在,phone:{}", loginRequest.getAccount());
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "此账号未注册");
        }
        if (!user.getStatus()) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "此账号被禁用");
        }
        String password = AESUtil.encryptCBC(loginRequest.getAccount(), loginRequest.getPassword());
        if (!user.getPwd().equals(password)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "密码错误");
        }
        return getLoginResponseAndToken(user);
    }

    private LoginResponse getLoginResponseAndToken(ChatUser user) {
        // 设置登录token
        LoginResponse loginResponse = getLoginResponse(user);

        // 记录最后一次登录时间和IP
        chatUserService.updateLastLoginIpTime(user.getId());
        return loginResponse;
    }

    private LoginResponse getLoginResponse(ChatUser user) {
        LoginResponse loginResponse = new LoginResponse();
        LoginUsername loginUsername = new LoginUsername(user.getId(), user.getPhone(), user.getNickname());
        TokenModel token = tokenManager.createToken(loginUsername);
        loginResponse.setToken(token.getToken());
        loginResponse.setExpiresIn(token.getExpiresIn());
        loginResponse.setLastAccessedTime(token.getLastAccessedTime());
        return loginResponse;
    }

    @Override
    public LoginResponse refreshToken() {
        TokenModel tokenModel = tokenManager.refreshToken();
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(tokenModel.getToken());
        loginResponse.setExpiresIn(tokenModel.getExpiresIn());
        loginResponse.setLastAccessedTime(tokenModel.getLastAccessedTime());
        return loginResponse;
    }

    /**
     * 退出
     *
     * @author Mr.Zhang
     * @since 2020-04-28
     */
    @Override
    public void logout() {
        try {
            tokenManager.deleteToken();
        } catch (Exception e) {
            log.warn("退出异常", e);
        }
    }

    @Override
    public Boolean updatePassword(String oldPwd, String newPwd) {
        LoginUsername loginUser = tokenManager.getLoginUser();
        return chatUserService.updatePassword(loginUser.getUid(), oldPwd, newPwd);
    }

    @Override
    public LoginResponse registerAndLogin(String phone, String password, String nickname) {
        ChatUser newUser = chatUserService.addNewUser(phone, password, nickname);
        redisUtil.set(CommConstant.CHAT_USER_FIRST_KEY + newUser.getId(), "1",5L);
        return getLoginResponseAndToken(newUser);
    }

}
