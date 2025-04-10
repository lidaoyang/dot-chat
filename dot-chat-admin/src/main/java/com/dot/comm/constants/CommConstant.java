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

    int DEFAULT_PAGE = 0;

    int DEFAULT_LIMIT = 20;

    String LAST_LATTER = "_";

    String REP_LATTER = "#";

    String CURR_CHAT_ID_KEY = "currChatId";

    String CURR_FRIEND_APPLY_ID_KEY = "currFriendApplyId";

     String UPLOAD_PROGRESSBAR_KEY = "upload:progressbar:data:";// 上传进度条数据

    String CHAT_GROUP_QRCODE_KEY = "chat:group:qrcode:";

    String DEFAULT_AVATAR = "https://oss.pinmallzj.com/image/maintain/2023/11/20/defaultAvator-d3f5ceb566fh.png";

    /**
     * 通话中Redis Key
     */
    String CHAT_MSG_CALLING_KEY = "chat:msg:calling:";

}
