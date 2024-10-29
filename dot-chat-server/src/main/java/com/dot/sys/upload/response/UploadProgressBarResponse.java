package com.dot.sys.upload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author: Dao-yang.
 * @date: Created in 2022/6/21 15:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "UploadProgressBarResponse", description = "上传文件进度条对象")
public class UploadProgressBarResponse {


    /**
     * 上传状态[STARTED,STARTED,COMPLETED,FAILED]
     */
    @Schema(description = "上传状态[NO_START,STARTED,UPLOADING,COMPLETED,FAILED]")
    private String status;

    /**
     * 上传文件总大小
     */
    @Schema(description = "上传文件总大小")
    private String totalBytes;

    /**
     * 已上传大小
     */
    @Schema(description = "已上传大小")
    private String bytesWritten;

    /**
     * 上传百分比
     */
    @Schema(description = "上传进度百分比")
    private String percent;


}
