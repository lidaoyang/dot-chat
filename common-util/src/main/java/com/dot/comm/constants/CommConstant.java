package com.dot.comm.constants;

/**
 * 公共常量
 *
 * @date: Created in 2023/9/26 15:36
 */
public interface CommConstant {

    /**
     * 链路ID
     */
    String TRACE_ID = "traceId";

    int DEFAULT_PAGE = 1;

    int DEFAULT_LIMIT = 20;


     String UPLOAD_PROGRESSBAR_KEY = "upload:progressbar:data:";// 上传进度条数据

    String DEFAULT_AVATAR = "https://oss.pinmallzj.com/image/maintain/2023/11/20/defaultAvator-d3f5ceb566fh.png";

    String REDIS_AUTH_ACCOUNT_LIST = "auth:account:list";

    String AUTH_ACCOUNT_KEY = "Auth-Account";
}
