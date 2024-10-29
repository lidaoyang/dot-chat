package com.dot.msg.chat.tio.entiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件消息对象(图片,视频,文件)
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/18 10:40
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 新文件名
     */
    private String newFileName;

    /**
     * 文件大小
     */
    private Integer fileSize;

    /**
     * 文件后缀
     */
    private String extName;

    /**
     * 文件路径
     */
    private String fileUrl;

    /**
     * 文件上传状态(1:上传完成;0:上传中)
     */
    private Integer status;

    /**
     * 视频封面路径
     */
    private String coverUrl;
}
