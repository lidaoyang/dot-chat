package com.dot.comm.interceptor;

import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSON;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.entity.ResultBean;
import com.dot.comm.manager.TokenManager;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * token验证拦截器
 */
public class AdminTokenInterceptor implements HandlerInterceptor {
    @Resource
    private TokenManager tokenManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        LoginUsername loginUser;
        try {
            loginUser = tokenManager.getLoginUser();
        } catch (Exception e) {
            response.getWriter().write(JSON.toJSONString(ResultBean.unauthorized()));
            return false;
        }
        // 演示管理员拒绝操作
        if (loginUser.isDemoAdmin() && !request.getMethod().equals(Method.GET.name())) {
            response.getWriter().write(JSON.toJSONString(ResultBean.forbidden()));
            return false;
        }
        return true;
    }
}
