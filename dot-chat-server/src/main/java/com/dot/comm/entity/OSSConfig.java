package com.dot.comm.entity;

import lombok.Data;

/**
 * 阿里云OSS文件信息
 */
@Data
public class OSSConfig {

    /**
     * 域名(图片或文件访问域名)
     */
    private String domain;

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * bucketName
     */
    private String bucketName;

    /**
     * 节点
     */
    private String region;

    /**
     * 是否上传到本地, 默认false, 为true时文件不在上传到OSS,直接保存到本地,domain为本地文件访问域名, accessKey, secretKey, bucketName, region无效 <br>
     * 注意: isLocal为true时, 文件将保存到本地,服务器迁移时需要一起迁移
     */
    private boolean local;
}
