package com.dot.comm.manager;

import cn.hutool.core.util.ObjectUtil;
import com.dot.comm.constants.TokenConstant;
import com.dot.comm.em.UserTypeEm;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.entity.TokenModel;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.RedisUtil;
import com.dot.comm.utils.RequestUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 口令管理
 */
@Component
public class TokenManager {

    @Resource(name = "redisUtil")
    protected RedisUtil redisUtil;


    public TokenModel createToken(LoginUsername loginUsername, String modelName) {
        return createToken(loginUsername.getAccount(), loginUsername.mergeUsername(), modelName);
    }

    /**
     * 生成Token
     *
     * @param account   String 账号
     * @param value     String 存储value
     * @param modelName String 模块
     * @author Mr.Zhang
     * @since 2020-04-29
     */

    public TokenModel createToken(String account, String value, String modelName) {
        // 获取一个随机UUID+3位随机串，定义为_token
        String _token = UUID.randomUUID().toString().replace("-", "") + RandomStringUtils.randomAlphabetic(3).toLowerCase();
        // 定义新TokenModel类，构造函数中分别填入用户账号account和那个随机UUID _token
        TokenModel token = new TokenModel(account, _token);
        // TokenModel对象token中设置用户号UserNo为用户账号
        token.setUserNo(account);
        // TokenModel对象token中设置最后一次登录时间为系统当前时间
        token.setLastAccessedTime(System.currentTimeMillis());
        // 设置有效期
        token.setExpiresIn(TokenConstant.TOKEN_EXPRESS_MINUTES * 60);

        // 将用户token加入缓存
        // 键: modelName (TOKEN:USER:) + _token (随机UUID值)
        // 值: value (用户uid)
        // 过期时间: TokenConstant.TOKEN_EXPRESS_MINUTES(3小时)
        // 时间格式: TimeUnit.MINUTES
        redisUtil.set(modelName + _token, value, TokenConstant.TOKEN_EXPRESS_MINUTES, TimeUnit.MINUTES);
        return token;
    }
    public TokenModel refreshToken(UserTypeEm userType) {
        HttpServletRequest req = RequestUtil.getRequest();
        if (ObjectUtil.isNull(req)) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "非法访问");
        }
        String token = req.getHeader(TokenConstant.HEADER_AUTHORIZATION_KEY);
        if (userType == UserTypeEm.ENT_USER) {
           return refreshToken(token, TokenConstant.TOKEN_USER_REDIS);
        } else {
            return refreshToken(token, TokenConstant.TOKEN_ADMIN_REDIS);
        }
    }

    public TokenModel refreshToken(String token, String modelName) {
        boolean exist = checkToken(token, modelName);
        if (!exist) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "token已过期，请重新登录！");
        }
        TokenModel tokenModel = new TokenModel();
        // 获取一个随机UUID+3位随机串，定义为_token
        String _token = UUID.randomUUID().toString().replace("-", "") + RandomStringUtils.randomAlphabetic(3).toLowerCase();
        tokenModel.setToken(_token);
        // 设置有效期
        tokenModel.setExpiresIn(TokenConstant.TOKEN_EXPRESS_MINUTES * 60);

        // 将用户token加入缓存
        // 键: modelName (TOKEN:USER:) + _token (随机UUID值)
        // 值: value (用户uid)
        // 过期时间: TokenConstant.TOKEN_EXPRESS_MINUTES(24小时)
        // 时间格式: TimeUnit.MINUTES
        String value = redisUtil.get(modelName + token);
        redisUtil.set(modelName + _token, value, TokenConstant.TOKEN_EXPRESS_MINUTES, TimeUnit.MINUTES);
        // 删除旧token
        // deleteToken(token, modelName);
        return tokenModel;
    }


    public Integer getUserIdByToken(String token) {
        LoginUsername tokenModel = getLoginUser(token, TokenConstant.TOKEN_USER_REDIS);
        return tokenModel.getUserId();
    }


    public Integer getUserId() {
        String token = Objects.requireNonNull(RequestUtil.getRequest()).getHeader(TokenConstant.HEADER_AUTHORIZATION_KEY);
        return getUserIdByToken(token);
    }

    public LoginUsername getLoginUser(UserTypeEm userType) {
        if (ObjectUtil.isNull(userType)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "用户类型不能为空");
        }
        LoginUsername loginUser;
        if (userType == UserTypeEm.ENT_USER) {
            loginUser = getLoginUserFront();
        } else {
            loginUser = getLoginUserAdmin();
        }
        return loginUser;
    }

    public LoginUsername getLoginUser(UserTypeEm userType, String token) {
        if (ObjectUtil.isNull(userType)) {
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "用户类型不能为空");
        }
        LoginUsername loginUser;
        if (userType == UserTypeEm.ENT_USER) {
            loginUser = getLoginUserFront(token);
        } else {
            loginUser = getLoginUserAdmin(token);
        }
        return loginUser;
    }

    public LoginUsername getLoginUserFront() {
        HttpServletRequest req = RequestUtil.getRequest();
        if (ObjectUtil.isNull(req)) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "非法访问");
        }
        String token = req.getHeader(TokenConstant.HEADER_AUTHORIZATION_KEY);
        return getLoginUserFront(token);
    }

    public LoginUsername getLoginUserFront(String token) {
        boolean tokenExist = checkToken(token, TokenConstant.TOKEN_USER_REDIS);
        if (!tokenExist) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "当前token无效");
        }
        return getLoginUser(token, TokenConstant.TOKEN_USER_REDIS);
    }


    public Integer getAdminId() {
        return getLoginUserAdmin().getUserId();
    }


    public LoginUsername getLoginUserAdmin() {
        HttpServletRequest req = RequestUtil.getRequest();
        if (ObjectUtil.isNull(req)) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "非法访问");
        }
        String token = req.getHeader(TokenConstant.HEADER_AUTHORIZATION_KEY);
        if (StringUtils.isBlank(token)) {
            token = req.getHeader(TokenConstant.HEADER_TOKEN_KEY);
        }
        return getLoginUserAdmin(token);
    }

    public LoginUsername getLoginUserAdmin(String token) {
        boolean tokenExist = checkToken(token, TokenConstant.TOKEN_ADMIN_REDIS);
        if (!tokenExist) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "当前token无效");
        }
        return getLoginUser(token, TokenConstant.TOKEN_ADMIN_REDIS);
    }

    /**
     * 检测Token
     *
     * @param token     String token
     * @param modelName String 模块
     * @author Mr.Zhang
     * @since 2020-04-29
     */

    public boolean checkToken(String token, String modelName) {
        return redisUtil.exists(modelName + token);
    }


    public String getTokenFormRequest() {
        HttpServletRequest request = RequestUtil.getRequest();
        assert request != null;
        return getTokenFormRequest(request);
    }


    public String getTokenFormRequest(HttpServletRequest request) {
        String pathToken = request.getParameter(TokenConstant.HEADER_AUTHORIZATION_KEY);
        if (null != pathToken) {
            return pathToken;
        }
        return request.getHeader(TokenConstant.HEADER_AUTHORIZATION_KEY);
    }

    public void deleteToken(UserTypeEm userType) {
        HttpServletRequest req = RequestUtil.getRequest();
        if (ObjectUtil.isNull(req)) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "非法访问");
        }
        String token = req.getHeader(TokenConstant.HEADER_AUTHORIZATION_KEY);
        deleteToken(userType, token);
    }

    public void deleteToken(UserTypeEm userType, String token) {
        if (userType == UserTypeEm.ENT_USER) {
            deleteToken(token, TokenConstant.TOKEN_USER_REDIS);
        } else {
            deleteToken(token, TokenConstant.TOKEN_ADMIN_REDIS);
        }
    }

    /**
     * 删除Token
     *
     * @param token     String token
     * @param modelName String 模块
     * @author Mr.Zhang
     * @since 2020-04-29
     */

    public void deleteToken(String token, String modelName) {
        boolean tokenExist = checkToken(token, modelName);
        if (!tokenExist) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "当前token无效");
        }
        redisUtil.remove(modelName + token);
    }


    public LoginUsername getLoginUser(String token, String modelName) {
        String strVal = redisUtil.get(modelName + token);
        if (StringUtils.isBlank(strVal)) {
            throw new ApiException(ExceptionCodeEm.UNAUTHORIZED, "当前token无效");
        }
        strVal = strVal.replace("\"", "");
        return LoginUsername.splitUsername(strVal);
    }

}
