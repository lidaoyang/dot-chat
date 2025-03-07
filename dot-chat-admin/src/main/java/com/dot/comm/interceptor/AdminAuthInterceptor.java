package com.dot.comm.interceptor;

import com.alibaba.fastjson2.JSONObject;
import com.dot.comm.entity.ResultBean;
import com.dot.comm.utils.RequestUtil;
import com.dot.sys.auth.service.SysRoleService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * token验证拦截器
 */

// 管理员拦截器
public class AdminAuthInterceptor implements HandlerInterceptor {
    @Resource
    private SysRoleService sysRoleService;

    // 程序处理之前需要处理的业务
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String uri = RequestUtil.getUri(request);
        if (uri == null || uri.isEmpty()) {
            response.getWriter().write(JSONObject.toJSONString(ResultBean.forbidden()));
            return false;
        }
        Boolean result = sysRoleService.checkUrlAccessPermissions(uri);
        if (!result) {
            response.getWriter().write(JSONObject.toJSONString(ResultBean.forbidden()));
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {

    }

}
