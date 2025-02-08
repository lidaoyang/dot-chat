package com.dot.comm.utils.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Http请求返回对象
 *
 * @author: Dao-yang.
 * @date: Created in 2022/4/10 13:53
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class HttpResponseVo {
    
    private String httpBody;

    private byte[] bytesBody;
    
    private String method;
    
    private int status;
    
    private String code;
    
    private JSONObject jsonObject;
    
}
