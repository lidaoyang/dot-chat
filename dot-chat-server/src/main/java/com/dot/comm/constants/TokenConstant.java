package com.dot.comm.constants;

/**
 * token操作常量
 *
 * @author: Meng-meng.
 * @date: Created in 2023/9/26 15:36
 */
public interface TokenConstant {

    /**
     * 管理员token redis key
     */
    String TOKEN_ADMIN_REDIS = "TOKEN:ADMIN:";

    /**
     * 用户缓存token redis key
     */
    String TOKEN_USER_REDIS = "TOKEN:USER:";

    /**
     * 刷新token redis key
     */
    String TOKEN_REFRESH_REDIS = "TOKEN:REFRESH:";

    /**
     * token有效期(分钟)-(60 * 3); // 3小时;
     */
    long TOKEN_EXPRESS_MINUTES = (60 * 3); // 3小时;

    /**
     * 头部 token令牌key
     */
    String HEADER_AUTHORIZATION_KEY = "Authorization";

    String HEADER_ORIGIN_KEY = "Origin";

    String HEADER_TOKEN_KEY = "token";

}
