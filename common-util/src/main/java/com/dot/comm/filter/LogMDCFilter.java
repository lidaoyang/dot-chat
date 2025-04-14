package com.dot.comm.filter;

import cn.hutool.core.util.IdUtil;
import com.dot.comm.constants.ComConstant;
import com.dot.comm.utils.CommUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * @author: Dao-yang.
 * @date: Created in 2023/5/16 15:06
 */
@Slf4j
public class LogMDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        if (!requestURI.startsWith("/api/")) { // 静态文件直接放行
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String traceId = getRequestId(httpServletRequest);
        String clientIp = CommUtil.getClientIp(httpServletRequest);
        log.info("-{}-{}-{}-请求开始", traceId, clientIp, requestURI);
        MDC.put(ComConstant.TRACE_ID, traceId);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            log.info("-{}-{}-请求结束", clientIp, requestURI);
            MDC.remove(ComConstant.TRACE_ID);
        }
    }

    public String getRequestId(HttpServletRequest request) {
        String traceId;
        String parameterRequestId = request.getParameter(ComConstant.TRACE_ID);
        String headerRequestId = request.getHeader(ComConstant.TRACE_ID);
        // 根据请求参数或请求头判断是否有“request-id”，有则使用，无则创建
        if (parameterRequestId == null && headerRequestId == null) {
            traceId = IdUtil.simpleUUID();
        } else {
            traceId = parameterRequestId != null ? parameterRequestId : headerRequestId;
        }
        return traceId;
    }
}
