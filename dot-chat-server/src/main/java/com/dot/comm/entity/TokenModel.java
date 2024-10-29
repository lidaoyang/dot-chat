package com.dot.comm.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

/**
 * token实体类


 */
@Data
public class TokenModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 4903514237492573024L;

    // 用户号
    private String userNo;
    private Integer userId;
    private String token;
    // 最后访问时间
    private long lastAccessedTime = System.currentTimeMillis();
    // 凭证有效时间，单位：秒。
    private Long expiresIn;
    // 客户端类型
    private String clienttype;
    // 客户端语言
    private Locale locale;
    // 客户端ip
    private String host;

    public TokenModel(String userno, String token){
        this.userNo = userno;
        this.token = token;
    }

    public TokenModel() {
    }
}
