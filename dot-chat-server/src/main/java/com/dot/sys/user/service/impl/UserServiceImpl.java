package com.dot.sys.user.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.constants.TokenConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.em.UserTypeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.entity.TokenModel;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
import com.dot.comm.utils.CommUtil;
import com.dot.comm.utils.RedisUtil;
import com.dot.sys.user.dao.UserDao;
import com.dot.sys.user.model.SystemAdmin;
import com.dot.sys.user.model.User;
import com.dot.sys.user.request.LoginRequest;
import com.dot.sys.user.response.LoginResponse;
import com.dot.sys.user.service.SystemAdminService;
import com.dot.sys.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    @Resource
    private SystemAdminService systemAdminService;

    @Resource
    private TokenManager tokenManager;

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        if (StringUtils.isBlank(loginRequest.getPassword())) {
            throw new ApiException(ExceptionCodeEm.PRAM_NOT_MATCH, "密码不能为空");
        }
        String password = CommUtil.encryptPassword(loginRequest.getPassword(), loginRequest.getAccount());
        User user = getUser(loginRequest);
        if (ObjectUtil.isNull(user)) {
            log.error("账号不存在,phone:{}", loginRequest.getAccount());
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "此账号未注册");
        }
        if (!user.getStatus()) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "此账号被禁用");
        }

        if (!user.getPwd().equals(password)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "密码错误");
        }
        return getLoginResponseAndToken(loginRequest.getUserType(), user);
    }

    private LoginResponse getLoginResponseAndToken(UserTypeEm userType, User user) {
        LoginResponse loginResponse = new LoginResponse();
        // 设置登录token
        setLoginToken(userType, user, loginResponse);

        // 记录最后一次登录时间
        updateLastLoginTime(userType, user);

        String domain = "https://oss.pinmallzj.com/";
        loginResponse.setUid(user.getUid());
        loginResponse.setNikeName(StringUtils.isBlank(user.getNickname()) ? user.getAccount() : user.getNickname());
        if (StringUtils.isBlank(user.getAvatar())) {
            loginResponse.setAvatar(CommConstant.DEFAULT_AVATAR);
        } else {
            loginResponse.setAvatar(user.getAvatar().startsWith("http") ? user.getAvatar() : domain + user.getAvatar());
        }
        loginResponse.setEnterpriseId(user.getEnterpriseId());
        return loginResponse;
    }

    private void updateLastLoginTime(UserTypeEm userType, User user) {
        if (userType == UserTypeEm.ENT_USER) {
            LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(User::getUid, user.getUid());
            updateWrapper.set(User::getLastLoginTime, DateUtil.now());
            updateWrapper.set(User::getLastIp, CommUtil.getClientIp());
            this.update(updateWrapper);
        } else {
            LambdaUpdateWrapper<SystemAdmin> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(SystemAdmin::getId, user.getUid());
            updateWrapper.set(SystemAdmin::getLastTime, DateUtil.now());
            updateWrapper.set(SystemAdmin::getLastIp, CommUtil.getClientIp());
            systemAdminService.update(updateWrapper);
        }
    }

    private User getUser(LoginRequest loginRequest) {
        User user;
        if (loginRequest.getUserType() == UserTypeEm.ENT_USER) {
            user = getUser(loginRequest.getEnterpriseId(), loginRequest.getAccount());
        } else {
            SystemAdmin admin = systemAdminService.getAdmin(loginRequest.getUserType().getCode(), loginRequest.getEnterpriseId(), loginRequest.getAccount());
            if (ObjectUtil.isNull(admin)) {
                log.error("账号不存在,loginRequest:{}", JSON.toJSONString(loginRequest));
                throw new ApiException(ExceptionCodeEm.NOT_FOUND, "此账号未注册");
            }
            user = new User();
            user.setUid(admin.getId());
            user.setAccount(admin.getAccount());
            user.setNickname(admin.getRealName());
            user.setAvatar(admin.getAvatar());
            user.setPwd(admin.getPwd());
            user.setUserType("0");
            user.setStatus(admin.getStatus());
            user.setEnterpriseId(admin.getEnterpriseId());
            user.setIndustryType(admin.getIndustryType());
        }
        return user;
    }

    private void setLoginToken(UserTypeEm userType, User user, LoginResponse loginResponse) {
        LoginUsername loginUsername =
                new LoginUsername(userType.getCode(), user.getAccount(), user.getIndustryType(), user.getEnterpriseId(), user.getUid(), user.getUserType());
        String modeName = userType == UserTypeEm.ENT_USER ? TokenConstant.TOKEN_USER_REDIS : TokenConstant.TOKEN_ADMIN_REDIS;
        TokenModel token = tokenManager.createToken(loginUsername, modeName);
        loginResponse.setToken(token.getToken());
        loginResponse.setExpiresIn(token.getExpiresIn());
        loginResponse.setLastAccessedTime(token.getLastAccessedTime());
    }

    @Override
    public LoginResponse refreshToken(UserTypeEm userType) {
        TokenModel tokenModel = tokenManager.refreshToken(userType);
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
    public void logout(UserTypeEm userType) {
        try {
            tokenManager.deleteToken(userType);
        } catch (Exception e) {
            log.warn("退出异常", e);
        }
    }

    @Override
    public User getUser(Integer enterpriseId, String phone) {
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getEnterpriseId, enterpriseId);
        lqw.eq(User::getAccount, phone);
        return this.getOne(lqw);
    }

    @Override
    public List<Integer> getEnterpriseIdList(String account) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(User::getEnterpriseId);
        queryWrapper.eq(User::getAccount, account);
        return this.listObjs(queryWrapper);
    }

    @Override
    public Boolean updatePassword(UserTypeEm userType, String oldPwd, String newPwd) {
        LoginUsername loginUser = tokenManager.getLoginUser(userType);
        if (loginUser.isEntUser()) {
            User user = this.getById(loginUser.getUserId());
            checkPwd(user.getPwd(), oldPwd, newPwd, user.getAccount(), userType);
            LambdaUpdateWrapper<User> lqw = Wrappers.lambdaUpdate();
            lqw.eq(User::getUid, loginUser.getUserId());
            lqw.set(User::getPwd, CommUtil.encryptPassword(newPwd, loginUser.getAccount()));
            return this.update(lqw);
        } else {
            SystemAdmin admin = systemAdminService.getById(loginUser.getUserId());
            checkPwd(admin.getPwd(), oldPwd, newPwd, admin.getAccount(), userType);
            LambdaUpdateWrapper<SystemAdmin> lqw = Wrappers.lambdaUpdate();
            lqw.eq(SystemAdmin::getId, loginUser.getUserId());
            lqw.set(SystemAdmin::getPwd, CommUtil.encryptPassword(newPwd, admin.getAccount()));
            return systemAdminService.update(lqw);
        }
    }

    private void checkPwd(String pwd, String oldPwd, String newPwd, String account, UserTypeEm userType) {
        if (!pwd.equals(CommUtil.encryptPassword(oldPwd, account))) {
            log.error("原密码错误,account:{},userType:{}", account, userType);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "原密码错误");
        }
        if (pwd.equals(CommUtil.encryptPassword(newPwd, account))) {
            log.error("新密码不能与原密码相同,account:{},userType:{}", account, userType);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "新密码不能与原密码相同");
        }
    }

    @Override
    public LoginResponse registerAndLogin(String phone, String password, String nickname) {
        if (isExist(phone)) {
            log.error("账号已存在,phone:{}", phone);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "账号已存在");
        }
        User user = new User();
        user.setAccount(phone);
        user.setPwd(CommUtil.encryptPassword(password, phone));
        if (StringUtils.isBlank(nickname)) {
            nickname = phone;
        }
        user.setNickname(nickname);
        user.setEnterpriseId(30);
        user.setIndustryType(1);
        user.setAvatar(CommConstant.DEFAULT_AVATAR);
        boolean saved = this.save(user);
        if (!saved) {
            log.error("注册失败,phone:{}", phone);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "注册失败");
        }
        return getLoginResponseAndToken(UserTypeEm.ENT_USER, user);
    }

    private boolean isExist(String phone) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(User::getUid);
        queryWrapper.eq(User::getAccount, phone);
        queryWrapper.eq(User::getEnterpriseId, 30);
        return ObjectUtil.isNotNull(this.getOne(queryWrapper));
    }
}
