package com.dot.sys.upload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件信息
 */
@Data
public class UploadResponse {

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 上传后产生的新文件名
     */
    @Schema(description = "新文件名,上传后产生的新文件名")
    private String newFileName;

    /**
     * 文件扩展名
     */
    @Schema(description = "扩展名")
    private String extName;

    /**
     * 文件大小，字节
     */
    @Schema(description = "文件大小，字节")
    private Long fileSize;

    /**
     * 文件存储在服务器的地址
     */
    @Schema(description = "文件存储在服务器的地址")
    private String serverPath;

    /**
     * 可供访问的url
     */
    @Schema(description = "文件上传路径")
    private String uploadPath;

    /**
     * 可供访问的url
     */
    @Schema(description = "可供访问的url")
    private String url;

    /**
     * 上传视频时返回的封面URL
     */
    @Schema(description = "上传视频时返回的封面URL")
    private String coverUrl;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private String type;
}
