package com.dot.sys.upload.service.impl;

import cn.hutool.core.util.StrUtil;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.OSSConfig;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.*;
import com.dot.sys.upload.config.UploadFileConfig;
import com.dot.sys.upload.response.UploadProgressBarResponse;
import com.dot.sys.upload.response.UploadResponse;
import com.dot.sys.upload.service.UploadService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author: Dao-yang.
 * @date: Created in 2024/4/3 10:54
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Resource
    private OSSConfig ossConfig;

    @Resource(name = "imageConfig")
    private UploadFileConfig imageConfig;

    @Resource(name = "videoConfig")
    private UploadFileConfig videoConfig;

    @Resource(name = "fileConfig")
    private UploadFileConfig fileConfig;

    @Resource(name = "redisUtil")
    protected RedisUtil redisUtil;

    private final String uploadType = "attachment";

    @Override
    public UploadResponse uploadImageAsync(MultipartFile multipart, String model) {
        return uploadImage(multipart, model, true);
    }

    @Override
    public UploadResponse uploadImage(MultipartFile multipart, String model) {
        return uploadImage(multipart, model, false);
    }

    @Override
    public UploadResponse uploadImage(MultipartFile multipart, String model, boolean isAsync) {
        return uploadFile(multipart, imageConfig, model, isAsync);
    }

    @Override
    public UploadResponse uploadFileAsync(MultipartFile multipart, String model) {
        return uploadFile(multipart, model, true);
    }

    @Override
    public UploadResponse uploadFile(MultipartFile multipart, String model) {
        return uploadFile(multipart, model, false);
    }

    @Override
    public UploadResponse uploadFile(MultipartFile multipart, String model, boolean isAsync) {
        return uploadFile(multipart, fileConfig, model, isAsync);
    }

    @Override
    public UploadResponse uploadVideoAsync(MultipartFile multipart, String model) {
        UploadResponse response = getUploadResponse(multipart, videoConfig, model);
        // 上传视频封面
        uploadVideoCoverImg(multipart, model, response);
        // 上传视频文件
        upload(multipart, true, response);
        return response;
    }

    /**
     * 上传视频封面
     *
     * @param multipart      文件
     * @param model          model
     * @param uploadResponse res
     */
    private void uploadVideoCoverImg(MultipartFile multipart, String model, UploadResponse uploadResponse) {
        InputStream imageInputStream = VideoUtil.coverImageInputStream(1, multipart);
        String newFileName = uploadResponse.getNewFileName().substring(0, uploadResponse.getNewFileName().lastIndexOf(".")) + ".jpg";
        String uploadPath = UploadUtil.getUploadPath(videoConfig.getRootContext(), videoConfig.getType(), model) + newFileName;
        if (ossConfig.isLocal()) {
            uploadResponse.setStatus(1);
            String targetPath = videoConfig.getRootPath() + "/" + UploadUtil.getUploadPath(videoConfig.getType(), model) + newFileName;
            log.info("文件流保存到本地,targetPath:{}", targetPath);
            FileUtil.saveToFile(imageInputStream, targetPath);
        } else {
            // 上传图片到oss
            OSSUtil.getInstance(redisUtil, ossConfig).upload(imageInputStream, uploadPath, newFileName, uploadType);
        }
        String coverUrl = ossConfig.getDomain() + "/" + uploadPath;
        uploadResponse.setCoverUrl(coverUrl);
    }

    private UploadResponse uploadFile(MultipartFile multipart, UploadFileConfig uploadConfig, String model,
                                      boolean isAsync) {
        if (null == multipart || multipart.isEmpty()) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传的文件对象不存在...");
        }
        UploadResponse response = getUploadResponse(multipart, uploadConfig, model);
        upload(multipart, isAsync, response);
        return response;
    }

    private void upload(MultipartFile multipart, boolean isAsync, UploadResponse response) {
        try {
            // 创建文件
            File file = FileUtil.createFile(response.getServerPath());
            // 保存文件到本地
            multipart.transferTo(file);
            // 本地不上传OSS
            if (ossConfig.isLocal()) {
                response.setStatus(1);
                return;
            }
            // 上传文件到oss
            if (isAsync) {// 异步上传带进度条
                OSSUtil.getInstance(redisUtil, ossConfig).upload(file, response.getUploadPath(), uploadType);
            } else {// 同步上传
                OSSUtil.getInstance(ossConfig).upload(file, response.getUploadPath());
                response.setStatus(1);
            }
        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传文件失败" + e.getMessage());
        }
    }

    private UploadResponse getUploadResponse(MultipartFile multipart, UploadFileConfig uploadConfig, String model) {
        // 文件名
        String fileName = multipart.getOriginalFilename();
        log.info("fileName:{}", fileName);
        // 文件后缀名
        String extName = FilenameUtils.getExtension(fileName);
        if (StringUtils.isEmpty(extName)) {
            throw new RuntimeException("文件类型未定义不能上传...");
        }
        // 文件大小验证
        UploadUtil.checkSize(multipart.getSize(), uploadConfig.getSize());

        // 判断文件的后缀名是否符合规则
        UploadUtil.isContains(extName, uploadConfig.getExtStr());

        if (fileName.length() > 99) {
            fileName = StrUtil.subPre(fileName, 90).concat(".").concat(extName);
        }
        String shortName = fileName.replace("." + extName, "");
        // 新文件名
        String newFileName = UploadUtil.fileName(shortName, extName);

        String localFilePath = UploadUtil.getFullPath(uploadConfig.getRootPath(), uploadConfig.getType(), model) + newFileName;
        String uploadPath = UploadUtil.getUploadPath(uploadConfig.getRootContext(), uploadConfig.getType(), model) + newFileName;
        return getUploadResponse(multipart, fileName, newFileName, extName, localFilePath, uploadPath);
    }

    private UploadResponse getUploadResponse(MultipartFile multipart, String fileName, String newFileName,
                                             String extName,
                                             String localFilePath, String uploadPath) {
        UploadResponse result = new UploadResponse();
        String url = ossConfig.getDomain() + "/" + uploadPath;
        result.setFileSize(multipart.getSize());
        result.setFileName(fileName);
        result.setNewFileName(newFileName);
        result.setExtName(extName);
        result.setServerPath(localFilePath);
        result.setUploadPath(uploadPath);
        result.setUrl(url);
        result.setType(multipart.getContentType());
        return result;
    }

    @Override
    public UploadProgressBarResponse getUploadProgressBar(String fileName) {
        UploadProgressBarResponse progressBarVo = new UploadProgressBarResponse();
        if (StringUtils.isEmpty(fileName)) {
            throw new ApiException(ExceptionCodeEm.PRAM_NOT_MATCH, "文件名称不能为空");
        }
        String key = CommConstant.UPLOAD_PROGRESSBAR_KEY + uploadType + ":" + fileName;
        if (!redisUtil.exists(key)) {
            progressBarVo.setStatus("NO_START");
            return progressBarVo;
        }
        String status = String.valueOf(redisUtil.hmGet(key, "status"));
        String totalBytes = String.valueOf(redisUtil.hmGet(key, "totalBytes"));
        String bytesWritten = String.valueOf(redisUtil.hmGet(key, "bytesWritten"));
        String percent = String.valueOf(redisUtil.hmGet(key, "percent"));
        progressBarVo.setPercent(percent);
        progressBarVo.setStatus(status);
        progressBarVo.setTotalBytes(totalBytes);
        progressBarVo.setBytesWritten(bytesWritten);
        if ("COMPLETED".equals(status)) {
            log.info("上传PPT完成");
        }
        return progressBarVo;
    }

}
