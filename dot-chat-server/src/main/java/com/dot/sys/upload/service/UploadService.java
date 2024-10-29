package com.dot.sys.upload.service;

import com.dot.sys.upload.response.UploadProgressBarResponse;
import com.dot.sys.upload.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件服务
 *
 * @author: Dao-yang.
 * @date: Created in 2024/4/3 10:51
 */
public interface UploadService {

    /**
     * 异步上传图片
     *
     * @param multipart 文件
     * @param model     模块
     * @return 返回上传结果
     */
    UploadResponse uploadImageAsync(MultipartFile multipart, String model);

    /**
     * 同步上传图片
     *
     * @param multipart 文件
     * @param model     模块
     * @return 返回上传结果
     */
    UploadResponse uploadImage(MultipartFile multipart, String model);

    /**
     * 上传图片
     *
     * @param multipart 文件
     * @param model     模块
     * @param isAsync   是否异步
     * @return 返回上传结果
     */
    UploadResponse uploadImage(MultipartFile multipart, String model, boolean isAsync);

    /**
     * 异步上传文件
     *
     * @param multipart 文件对象
     * @param model     模块
     * @return 返回上传结果
     */
    UploadResponse uploadFileAsync(MultipartFile multipart, String model);

    /**
     * 同步上传文件
     *
     * @param multipart 文件对象
     * @param model     模块
     * @return 返回上传结果
     */
    UploadResponse uploadFile(MultipartFile multipart, String model);

    /**
     * 上传文件
     *
     * @param multipart 文件对象
     * @param model     模块
     * @param isAsync   是否异步
     * @return 返回上传结果
     */
    UploadResponse uploadFile(MultipartFile multipart, String model, boolean isAsync);

    /**
     * 异步上传视频
     *
     * @param multipart 文件
     * @param model     模块
     * @return 返回上传结果
     */
    UploadResponse uploadVideoAsync(MultipartFile multipart, String model);

    /**
     * 获取上传进度条
     *
     * @param fileName 文件名称
     * @return 上传进度
     */
    UploadProgressBarResponse getUploadProgressBar(String fileName);
}
