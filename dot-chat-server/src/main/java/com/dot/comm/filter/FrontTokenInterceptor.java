package com.dot.comm.filter;

import com.alibaba.fastjson.JSON;
import com.dot.comm.constants.TokenConstant;
import com.dot.comm.entity.ResultBean;
import com.dot.comm.manager.TokenManager;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  移动端管理端 token验证拦截器 使用前注意需要一个@Bean手动注解，否则注入无效

 */

// 前端用户拦截器
public class FrontTokenInterceptor implements HandlerInterceptor {
    @Resource
    private TokenManager tokenManager;

    //程序处理之前需要处理的业务
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = tokenManager.getTokenFormRequest(request);
        // token存在时，校验token
        boolean result = tokenManager.checkToken(TokenConstant.TOKEN_USER_REDIS,token);
        if(!result){
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            // response中写入未登录信息
            response.getWriter().write(JSON.toJSONString(ResultBean.unauthorized()));
            return false;
        }
        return true;
    }
}
