package com.dot.deepseek.utils;

import com.alibaba.fastjson.JSONObject;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.HttpClientUtil;
import com.dot.comm.utils.vo.HttpResponseVo;
import com.dot.deepseek.constants.DSConstant;
import com.dot.deepseek.entity.DSChatRequestBody;
import com.dot.deepseek.entity.DSChatRequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI 工具类
 *
 * @author: Dao-yang.
 * @date: Created in 2025/2/6 10:44
 */
@Slf4j
public class DSUtils {

    public static String generateChatMessage(List<DSChatRequestMessage> messages) {
        String url = getDSChatUrl();
        Map<String, String> header = HttpClientUtil.defaultHeader();
        header.put("Authorization", "Bearer " + getAPIKey());
        DSChatRequestBody requestBody = new DSChatRequestBody();
        requestBody.setMessages(messages);
        requestBody.setMax_tokens(128);
        HttpResponseVo responseVo = HttpClientUtil.doPost(url, header, JSONObject.toJSONString(requestBody));
        log.info("deepseek-chat-response:{}", responseVo.getHttpBody());
        if (responseVo.getStatus() == 200 && StringUtils.isNotBlank(responseVo.getHttpBody())) {
            JSONObject jsonObject = JSONObject.parseObject(responseVo.getHttpBody());
            return jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }
        return "服务器繁忙,请稍后重试";
    }

    public static void generateChatMessageForStream(List<DSChatRequestMessage> messages, Integer userId) {
        String url = getDSChatUrl();
        Map<String, String> header = HttpClientUtil.defaultHeader();
        header.put("Authorization", "Bearer " + getAPIKey());
        DSChatRequestBody requestBody = new DSChatRequestBody();
        requestBody.setMessages(messages);
        requestBody.setMax_tokens(1024);
        requestBody.setStream(true);
        HttpClientUtil.doPostForStream(url, header, JSONObject.toJSONString(requestBody), userId);
    }

    public static String getDSChatUrl() {
        return DSConstant.BASE_URL + DSConstant.CHAT_COMPLETION;
    }

    private static String getAPIKey() {
        String apiKey = System.getenv(DSConstant.DS_API_EVN_KEY);
        if (StringUtils.isBlank(apiKey)) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "未配置环境变量：" + DSConstant.DS_API_EVN_KEY);
        }
        return apiKey;
    }
}
