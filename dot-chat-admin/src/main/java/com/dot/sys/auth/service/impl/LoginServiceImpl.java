package com.dot.sys.auth.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.dot.chat.model.ChatUser;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.entity.TokenModel;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
import com.dot.comm.utils.AESUtil;
import com.dot.comm.utils.CommUtil;
import com.dot.comm.utils.RedisUtil;
import com.dot.sys.auth.model.SysAdmin;
import com.dot.sys.auth.request.LoginRequest;
import com.dot.sys.auth.response.LoginResponse;
import com.dot.sys.auth.response.SysAdminLoginResponse;
import com.dot.sys.auth.service.LoginService;
import com.dot.sys.auth.service.SysAdminService;
import com.dot.sys.auth.service.SysRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author: Dao-yang.
 * @date: Created in 2025/3/10 15:13
 */
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private SysAdminService adminService;

    @Resource
    private SysRoleService roleService;

    @Resource
    private TokenManager tokenManager;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        SysAdmin admin = adminService.getByAccount(loginRequest.getAccount());
        if (ObjectUtil.isNull(admin)) {
            log.error("账号不存在,account:{}", loginRequest.getAccount());
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "此账号未注册");
        }
        if (!admin.getStatus()) {
            log.error("此账号被禁用,account:{}", loginRequest.getAccount());
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "此账号被禁用");
        }
        String password = AESUtil.encryptCBC(loginRequest.getAccount(), loginRequest.getPassword());
        if (!admin.getPwd().equals(password)) {
            log.error("密码错误,account:{}", loginRequest.getAccount());
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "密码错误");
        }
        return getLoginResponseAndToken(admin);
    }

    private LoginResponse getLoginResponseAndToken(SysAdmin admin) {
        // 设置登录token
        LoginResponse loginResponse = getLoginResponse(admin);

        // 记录最后一次登录时间和IP
        adminService.updateLastLoginIpTime(admin.getId());
        return loginResponse;
    }

    private LoginResponse getLoginResponse(SysAdmin admin) {
        LoginResponse loginResponse = new LoginResponse();
        Integer roleType = roleService.getMaxRoleType(CommUtil.stringToArrayInt(admin.getRoles()));
        if (roleType == null) {
            log.error("当前用户角色被禁用或未配置,account:{},roles:{}", admin.getAccount(), admin.getRoles());
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "当前用户角色被禁用或未配置");
        }
        LoginUsername loginUsername = new LoginUsername(admin.getId(), admin.getAccount(), admin.getRoles(), roleType);
        TokenModel token = tokenManager.createToken(loginUsername);
        loginResponse.setToken(token.getToken());
        loginResponse.setExpiresIn(token.getExpiresIn());
        loginResponse.setLastAccessedTime(token.getLastAccessedTime());
        SysAdminLoginResponse adminLoginResponse = new SysAdminLoginResponse();
        adminLoginResponse.setId(admin.getId());
        adminLoginResponse.setAccount(admin.getAccount());
        adminLoginResponse.setName(admin.getName());
        adminLoginResponse.setAvatar(admin.getAvatar());
        adminLoginResponse.setRoleType(roleType);
        loginResponse.setUser(adminLoginResponse);
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
        return adminService.updatePassword(loginUser.getUid(), oldPwd, newPwd);
    }

}
