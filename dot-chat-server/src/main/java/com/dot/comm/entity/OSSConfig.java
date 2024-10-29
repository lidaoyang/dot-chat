package com.dot.comm.entity;

import lombok.Data;

/**
 * 文件信息


 */
@Data
public class OSSConfig {

    //域名空间
    private String domain;

    //accessKey
    private String accessKey;

    //secretKey
    private String secretKey;

    //bucketName
    private String bucketName;

    //节点
    private String region;
}
