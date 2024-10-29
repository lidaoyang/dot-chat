package com.dot.comm.progressbar;

import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author: Dao-yang.
 * @date: Created in 2022/6/22 10:11
 */
@Slf4j
public class PutObjectProgressListener implements ProgressListener {

    private final RedisUtil redisUtil;

    private final String fileName;

    private final String type;

    private long bytesWritten = 0;

    private long totalBytes = -1;

    public PutObjectProgressListener(RedisUtil redisUtil, String type, String fileName) {
        this.redisUtil = redisUtil;
        this.fileName = fileName;
        this.type = type;
    }

    @Override
    public void progressChanged(ProgressEvent event) {
        long bytes = event.getBytes();
        String redisKey = CommConstant.UPLOAD_PROGRESSBAR_KEY + type + ":" + this.fileName;
        // 缓存Redis 5分钟
        int expire = 5;
        ProgressEventType eventType = event.getEventType();
        switch (eventType) {
            case TRANSFER_STARTED_EVENT:
                log.info("Start to upload,fileName:{},totalBytes:{} ......", fileName, totalBytes);
                this.totalBytes = bytes;
                redisUtil.hmSet(redisKey, "status", "STARTED");
                redisUtil.hmSet(redisKey, "totalBytes", String.valueOf(totalBytes));
                redisUtil.expire(redisKey, expire, TimeUnit.MINUTES);
                break;

            case REQUEST_CONTENT_LENGTH_EVENT:
                this.totalBytes = bytes;
                log.info("fileName:{},totalBytes:{} bytes in total will be uploaded to OSS", this.fileName,
                    this.totalBytes);
                redisUtil.hmSet(redisKey, "status", "STARTING");
                redisUtil.hmSet(redisKey, "totalBytes", String.valueOf(totalBytes));
                redisUtil.expire(redisKey, expire, TimeUnit.MINUTES);
                break;

            case REQUEST_BYTE_TRANSFER_EVENT:
                this.bytesWritten += bytes;
                if (this.totalBytes != -1) {
                    double percent = (this.bytesWritten * 100.0 / this.totalBytes);
                    log.debug(
                        "fileName:{},written {} bytes have been written at this time, upload progress: {}% ({}/{})",
                        this.fileName, bytes, percent, this.bytesWritten, this.totalBytes);
                    redisUtil.hmSet(redisKey, "status", "UPLOADING");
                    redisUtil.hmSet(redisKey, "percent", String.format("%.2f", percent));
                    redisUtil.hmSet(redisKey, "bytesWritten", String.valueOf(bytesWritten));
                    redisUtil.hmSet(redisKey, "totalBytes", String.valueOf(totalBytes));
                    redisUtil.expire(redisKey, expire, TimeUnit.MINUTES);
                } else {
                    log.error("fileName:{},{} bytes have been written at this time, upload ratio: unknown({}/...)",
                        this.fileName, bytes, this.bytesWritten);
                }
                break;

            case TRANSFER_COMPLETED_EVENT:
                log.info("Succeed to upload, fileName:{},{} bytes have been transferred in total", this.fileName,
                    this.bytesWritten);
                redisUtil.hmSet(redisKey, "status", "COMPLETED");
                redisUtil.hmSet(redisKey, "bytesWritten", String.valueOf(bytesWritten));
                redisUtil.hmSet(redisKey, "totalBytes", String.valueOf(totalBytes));
                redisUtil.expire(redisKey, expire, TimeUnit.MINUTES);
                break;

            case TRANSFER_FAILED_EVENT:
                log.info("Failed to upload, fileName:{},{} bytes have been transferred", this.fileName,
                    this.bytesWritten);
                redisUtil.hmSet(redisKey, "status", "FAILED");
                redisUtil.hmSet(redisKey, "bytesWritten", String.valueOf(bytesWritten));
                redisUtil.hmSet(redisKey, "totalBytes", String.valueOf(totalBytes));
                break;

            default:
                break;
        }
    }
}
