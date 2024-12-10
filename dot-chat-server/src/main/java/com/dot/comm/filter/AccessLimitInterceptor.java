package com.dot.comm.filter;

import com.alibaba.fastjson.JSON;
import com.dot.comm.annotation.AccessLimit;
import com.dot.comm.constants.TokenConstant;
import com.dot.comm.entity.ResultBean;
import com.dot.comm.utils.CommUtil;
import com.dot.comm.utils.RedisUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author: Dao-yang.
 * @date: Created in 2022/12/20 10:19
 */
@Slf4j
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Resource
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 判断请求是否属于方法的请求
        if (handler instanceof HandlerMethod method) {
            // 默认1秒的访问最大次数为6次
            long seconds = 1;
            int maxCount = 6;
            // 获取方法中的注解,看是否有该注解
            AccessLimit accessLimit = method.getMethodAnnotation(AccessLimit.class);
            if (accessLimit != null) {
                seconds = accessLimit.seconds();
                maxCount = accessLimit.maxCount();
            }
            // 多缓存5秒
            seconds = seconds + 5;
            String key = getKey(request);

            // 从redis中获取用户访问的次数
            String strVal = redisUtil.get(key);
            long nowTime = System.currentTimeMillis();
            if (StringUtils.isBlank(strVal)) {
                log.info("第一次访问，key:{}", key);
                // 第一次访问 设置访问次数1  seconds秒释放
                String val = nowTime + ":" + 1;
                redisUtil.set(key, val, seconds, TimeUnit.SECONDS);
            } else {
                String[] vals = strVal.split(":");
                long time = Long.parseLong(vals[0]);
                int count = Integer.parseInt(vals[1]);
                if (time == 0) {
                    accessToLimitedRenderResponse(response, key, count);
                    return false;
                }
                if ((nowTime - time) > (seconds - 5) * 1000) {
                    redisUtil.remove(key);
                } else if (count < maxCount) {
                    count = count + 1;
                    String val = time + ":" + count;
                    redisUtil.set(key, val, seconds, TimeUnit.SECONDS);
                } else {
                    // 超出访问次数,超出接口时间段内允许访问的次数，直接返回错误信息,同时设置过期时间 15s自动剔除
                    String val = "0:" + count;
                    redisUtil.set(key, val, 15L, TimeUnit.SECONDS);
                    accessToLimitedRenderResponse(response, key, count);
                    return false;
                }
            }
        }

        return true;
    }

    private void accessToLimitedRenderResponse(HttpServletResponse response, String key, int count) throws Exception {
        log.warn("访问次数已达上限,暂停访问15秒,key:{},count:{}", key, count);
        ResultBean<Object> result = ResultBean.failed("访问次数已达上限！请稍后再访问");
        render(response, result);
    }

    private String getKey(HttpServletRequest request) {
        StringBuilder sbKey = new StringBuilder("ACCESS:");
        String ip = CommUtil.getClientIp(request);
        sbKey.append(ip).append(":");
        String url = request.getRequestURI();
        String referer = request.getHeader("Referer");
        if (StringUtils.isNotBlank(referer) && referer.startsWith("https://servicewechat.com/")) {// 小程序访问
            String appID = request.getHeader("AppID");
            sbKey.append(appID);
        } else {
            String origin = request.getHeader("origin");
            String orgHost = "unknown";
            if (StringUtils.isNotBlank(origin)) {
                orgHost = origin.substring(origin.indexOf("//") + 2);
            }
            sbKey.append(orgHost).append(":");
        }
        String token = request.getHeader(TokenConstant.HEADER_AUTHORIZATION_KEY);
        if (StringUtils.isNotEmpty(token)) {
            sbKey.append(token).append(":");
        }
        sbKey.append(url);
        return sbKey.toString();
    }

    private void render(HttpServletResponse response, ResultBean<Object> result) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(result);
        out.write(str.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }
}
