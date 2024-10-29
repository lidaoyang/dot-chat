package com.dot.comm.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Request工具类
 */
@Slf4j
public class RequestUtil extends HttpServlet {

    public static HttpServletRequest getRequest() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            return ((ServletRequestAttributes) Objects.requireNonNull(
                    RequestContextHolder.getRequestAttributes())).getRequest();
        }
        return null;
    }

    public static HashMap<String, Object> getRequestParamAndHeader() {

        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return null;
            }

            //request信息
            HashMap<String, Object> data = new HashMap<>();
            HashMap<String, Object> requestParams = new HashMap<>();
            Enumeration<String> paraNames = request.getParameterNames();
            if (paraNames != null) {
                for (Enumeration<String> enumeration = paraNames; enumeration.hasMoreElements(); ) {
                    String key = enumeration.nextElement();
                    requestParams.put(key, request.getParameter(key));
                }
            }

            HashMap<String, Object> requestFilter = new HashMap<>();
            Enumeration<String> attributeNames = request.getAttributeNames();
            if (attributeNames != null) {
                for (Enumeration<String> attributeNames1 = attributeNames; attributeNames1.hasMoreElements(); ) {
                    String key = attributeNames1.nextElement();
                    if (key.contains("request_")) {
                        requestFilter.put(key, request.getAttribute(key));
                    }
                }
            }

            data.put("url", request.getRequestURL());
            data.put("uri", request.getRequestURI());
            data.put("method", request.getMethod());
            data.put("request", requestParams);
            data.put("request_filter", requestFilter);

            //header 信息
            Enumeration<String> headerNames = request.getHeaderNames();
            HashMap<String, Object> headerParams = new HashMap<>();
            if (headerNames != null) {
                for (Enumeration<String> enumeration = headerNames; enumeration.hasMoreElements(); ) {
                    String key = enumeration.nextElement();
                    String value = request.getHeader(key);
                    headerParams.put(key, value);
                }
            }
            data.put("header", headerParams);

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDomain() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getServerName() + ":" + request.getServerPort();
    }

    public static String getUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        List<String> list = StrUtil.split(uri, "/", true, true);
        list.removeIf(StringUtils::isNumeric); //去掉url中的数字参数
        list.removeIf(c -> c.contains(","));// 去掉url中的逗号分隔参数
        return StringUtils.join(list, "/");
    }

    public static String getRequestData(HttpServletRequest request) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream inputStream = null;
        String content = "";
        try {
            inputStream = request.getInputStream();
            int c;
            while ((c = inputStream.read()) != -1) {
                os.write(c);
            }
            content = os.toString();
        } catch (IOException e) {
            log.error("读取数据流失败, msg:{}", e.getMessage(), e);
        } finally {
            try {
                os.close();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("关闭inputStream异常, msg:{}", e.getMessage());
            }
        }
        return content;
    }

    /**
     * 获取是否子商户
     *
     * @return
     */
    public static boolean isSubMini() {
        Object subMini = Objects.requireNonNull(getRequest()).getAttribute("subMini");
        if (ObjectUtil.isNull(subMini)) {
            return Boolean.FALSE;
        }
        return Boolean.parseBoolean(subMini.toString());
    }

    public static boolean isSubMini(String type) {
        if (StringUtils.isNotBlank(type) && type.startsWith("system")) {
            return "system-sub".equals(type);
        }
        return isSubMini();
    }

    public static String getAppID() {
        String appID = Objects.requireNonNull(getRequest()).getHeader("AppID");
        if (StringUtils.isBlank(appID)) {
            log.error("小程序APPID参数为空");
            throw new RuntimeException("登录过期，请重新进入小程序！");
        }
        return appID;
    }
}
