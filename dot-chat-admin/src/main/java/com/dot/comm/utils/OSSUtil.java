package com.dot.comm.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.OSSConfig;
import com.dot.comm.exception.ApiException;
import com.dot.comm.progressbar.PutObjectProgressListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: Dao-yang.
 * @date: Created in 2022/6/23 14:29
 */
@Slf4j
public class OSSUtil {

    private static OSSUtil instance;

    private RedisUtil redisUtil;

    private OSSConfig ossConfig;

    private static final ExecutorService pool =
            ThreadPoolUtil.createPool("upload-pool", 5, 200, 1000L, new LinkedBlockingQueue<>(1024));

    public static OSSUtil getInstance(RedisUtil redisUtil, OSSConfig ossConfig) {
        if (instance != null) {
            return instance;
        }
        return new OSSUtil(redisUtil, ossConfig);
    }

    public static OSSUtil getInstance(OSSConfig ossConfig) {
        if (instance != null) {
            return instance;
        }
        return new OSSUtil(ossConfig);
    }

    private OSSUtil(RedisUtil redisUtil, OSSConfig ossConfig) {
        this.redisUtil = redisUtil;
        this.ossConfig = ossConfig;
        checkOSSConfig();
    }

    private OSSUtil(OSSConfig ossConfig) {
        this.ossConfig = ossConfig;
        checkOSSConfig();
    }

    private void checkOSSConfig() {
        if (StringUtils.isAllBlank(ossConfig.getAccessKey(), ossConfig.getSecretKey())) {
            log.error("oss配置信息未定义,ossConfig:{}", ossConfig);
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "oss配置信息未定义...");
        }
    }

    private OSS getOSSClient() {
        OSS oss = new OSSClientBuilder().build(ossConfig.getRegion(), ossConfig.getAccessKey(),
                ossConfig.getSecretKey());
        // 设置Bucket的传输加速状态。
        // 当设置enabled为true时，表示开启传输加速；当设置enabled为false时，表示关闭传输加速。
        boolean enabled = true;
        oss.setBucketTransferAcceleration(ossConfig.getBucketName(), enabled);
        return oss;
    }

    public void upload(MultipartFile multipartFile, Integer userId, String type) {
        try {
            // 存储地址
            String dir = "file/user/uid-" + userId + "/";
            String fullName = dir + multipartFile.getOriginalFilename();
            upload(multipartFile.getInputStream(), fullName, type, multipartFile.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void upload(InputStream inputStream, String webFilePath, String fileName, String type) {
        try {
            // 初始化OSS
            OSS oss = getOSSClient();
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), webFilePath, inputStream)
                            .withProgressListener(new PutObjectProgressListener(redisUtil, type, fileName));
            // 设置限速5MB/s。10个文件平分5Mb上传限速 512KB
            setTrafficLimit(putObjectRequest);
            // 上传文件
            oss.putObject(putObjectRequest);
            oss.shutdown();
        } catch (Exception e) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传文件失败, msg:" + e.getMessage());
        }
    }

    public void upload(InputStream inputStream, String webFilePath) {
        try {
            // 初始化OSS
            OSS oss = getOSSClient();
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), webFilePath, inputStream);
            // 设置限速5MB/s。10个文件平分5Mb上传限速 512KB
            setTrafficLimit(putObjectRequest);
            // 上传文件
            oss.putObject(putObjectRequest);
            oss.shutdown();
        } catch (Exception e) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传文件失败, msg:" + e.getMessage());
        }
    }

    public void upload(File file, String uploadPath, String type) {
        try {
            pool.execute(() -> {
                log.info("开始异步上传文件,filename:{}", file.getName());
                // 初始化OSS
                OSS oss = getOSSClient();
                PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), uploadPath, file)
                        .withProgressListener(new PutObjectProgressListener(redisUtil, type, file.getName()));
                // 设置限速5MB/s。10个文件平分5Mb上传限速 512KB
                setTrafficLimit(putObjectRequest);
                // 上传文件
                oss.putObject(putObjectRequest);
                oss.shutdown();
                // 上传成功删除本地缓存文件
                deleteLocalCacheFile(file);
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传文件失败, msg:" + e.getMessage());
        }
    }

    private void deleteLocalCacheFile(File file) {
        log.info("上传成功，开始删除本地缓存");
        try {
            // 删除本地图片
            if (file.exists()) {
                if (!file.delete()) {
                    log.error("本地缓存文件删除失败! name:{},filePath:{}", file.getName(), file.getAbsolutePath());
                }
            } else {
                log.error("本地缓存文件不存在! name:{},filePath:{}", file.getName(), file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("本地缓存文件删除异常! filePath:{},msg:{}", file.getAbsolutePath(), e.getMessage());
            e.printStackTrace();
        }
    }

    public void upload(File file, String fileName, String uploadPath, String type) {
        try {
            pool.execute(() -> {
                log.info("开始异步上传文件,filename:{}", file.getName());
                // 初始化OSS
                OSS oss = getOSSClient();
                PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), uploadPath, file)
                        .withProgressListener(new PutObjectProgressListener(redisUtil, type, fileName));
                // 设置限速5MB/s。10个文件平分5Mb上传限速 512KB
                setTrafficLimit(putObjectRequest);
                // 上传文件
                oss.putObject(putObjectRequest);
                oss.shutdown();
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传文件失败, msg:" + e.getMessage());
        }
    }

    public void upload(File file, String uploadPath) {
        try {
            log.info("开始同上传文件,filename:{}", file.getName());
            // 初始化OSS
            OSS oss = getOSSClient();
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), uploadPath, file);
            // 设置限速5MB/s。10个文件平分5Mb上传限速 512KB
            setTrafficLimit(putObjectRequest);
            // 上传文件
            oss.putObject(putObjectRequest);
            oss.shutdown();
            // 上传成功删除本地缓存文件
            deleteLocalCacheFile(file);
        } catch (Exception e) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传文件失败, msg:" + e.getMessage());
        }
    }

    public void upload(MultipartFile multipartFile, String webFilePath, String type) {
        try {
            // 初始化OSS
            OSS oss = getOSSClient();
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(ossConfig.getBucketName(), webFilePath, multipartFile.getInputStream())
                            .withProgressListener(
                                    new PutObjectProgressListener(redisUtil, type, multipartFile.getOriginalFilename()));
            // 设置限速5MB/s。10个文件平分5Mb上传限速 512KB
            setTrafficLimit(putObjectRequest);
            // 上传文件
            oss.putObject(putObjectRequest);
            oss.shutdown();
        } catch (Exception e) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传文件失败, msg:" + e.getMessage());
        }
    }

    private void setTrafficLimit(PutObjectRequest putObjectRequest) {
        // 限速5 MB/s。1M=1024K，1Byte=8b；10个文件平分5Mb上传限速 512KB
        int limitSpeed = 5 * 1024 * 1024 * 8 / 10;
        putObjectRequest.setTrafficLimit(limitSpeed);
    }

    public void delete(String webFilePath) {
        try {
            webFilePath = clearPrefix(webFilePath, ossConfig.getDomain());
            // 初始化OSS
            OSS oss = getOSSClient();
            // 判断当前文件url 是否存在
            boolean exist = oss.doesObjectExist(ossConfig.getBucketName(), webFilePath);
            if (!exist) {
                log.warn("文件不存在,已删除该信息。filePath：{}，{}", ossConfig.getBucketName(), webFilePath);
            } else {
                // 删除文件。
                oss.deleteObject(ossConfig.getBucketName(), webFilePath);
                // 关闭OSSClient。
                oss.shutdown();
                log.info("删除成功，,{}", webFilePath);
            }
        } catch (OSSException | ClientException e) {
            e.printStackTrace();
            log.error("删除 oss 文件失败" + webFilePath);
        }
    }

    public String clearPrefix(String path, String cdnurl) {
        if (StringUtils.isBlank(path)) {
            return path;
        }
        if (path.contains(cdnurl + "/")) {
            return path.replace(cdnurl + "/", "");
        }
        return path;
    }

    public RedisUtil getRedisUtil() {
        return redisUtil;
    }

    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public OSSConfig getOssConfig() {
        return ossConfig;
    }

    public void setOssConfig(OSSConfig ossConfig) {
        this.ossConfig = ossConfig;
    }
}
