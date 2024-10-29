package com.dot.comm.utils;

import com.dot.comm.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 视频操作工具
 *
 * @author: Dao-yang.
 * @date: Created in 2024/6/14 09:34
 */
@Slf4j
public class VideoUtil {

    public static void main(String[] args) throws IOException, InterruptedException {
        VideoUtil.coverImageInputStream(1564, new File("/Users/daoyang/Desktop/聚如SaaS介绍.mp4"));
    }

    /**
     * 传入视频文件，生成对应的封面图文件
     *
     * @param frameNumber 帧数
     * @param file        视频文件
     */
    public static InputStream coverImageInputStream(Integer frameNumber, File file) {
        String vName = file.getName();
        Double videoSize = file.length() / 1024.0 / 1024.0;
        long timeMillis = System.currentTimeMillis();
        log.info("截取视频截图开始 视频名称:{} 视频大小:{}MB", vName, String.format("%.2f", videoSize));
        try (FileInputStream is = new FileInputStream(file);
             FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(is);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {
            return getByteArrayInputStream(frameNumber, grabber, converter, timeMillis);
        } catch (FrameGrabber.Exception e) {
            log.error("FrameGrabber.Exception :{}", e.getMessage());
        } catch (IOException e) {
            log.error("IOException :{}", e.getMessage());
        }
        return null;
    }

    /**
     * 传入视频文件，生成对应的封面图文件
     *
     * @param frameNumber   帧数
     * @param multipartFile 视频文件
     */
    public static InputStream coverImageInputStream(Integer frameNumber, MultipartFile multipartFile) {
        String vName = multipartFile.getOriginalFilename();
        Double videoSize = multipartFile.getSize() / 1024.0 / 1024.0;
        long timeMillis = System.currentTimeMillis();
        log.info("截取视频截图开始 视频名称:{} 视频大小:{}MB", vName, String.format("%.2f", videoSize));
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(multipartFile.getInputStream());
             Java2DFrameConverter converter = new Java2DFrameConverter()) {
            return getByteArrayInputStream(frameNumber, grabber, converter, timeMillis);
        } catch (FrameGrabber.Exception e) {
            log.error("FrameGrabber.Exception :{}", e.getMessage());
            throw new ApiException(e);
        } catch (IOException e) {
            log.error("IOException :{}", e.getMessage());
            throw new ApiException(e);
        }
    }

    private static ByteArrayInputStream getByteArrayInputStream(Integer frameNumber, FFmpegFrameGrabber grabber, Java2DFrameConverter converter, long timeMillis) throws IOException {
        grabber.start();
        // 视频的最大帧数，减少10帧是因为最后几帧会出现获取不到图片的情况
        int ftp = grabber.getLengthInFrames() - 10;
        frameNumber = ftp < frameNumber ? ftp : frameNumber;
        // 设置视频指定帧数
        grabber.setFrameNumber(frameNumber);
        log.info("视频的格式：{}", grabber.getFormat());
        log.info("视频总帧数：{}", grabber.getLengthInFrames());
        log.info("视频时长（s/秒）：{}", grabber.getLengthInTime() / (1000 * 1000));
        // 设置视频截取帧（默认取第一帧）
        Frame frame = grabber.grabImage();
        // 绘制图片
        BufferedImage outImage = converter.getBufferedImage(frame);
        // 输出文件
        // File imageFile = new File("/Users/daoyang/Desktop/image/test"+ RandomUtil.randomNumbers(4) +".png");
        // ImageIO.write(outImage, "png", imageFile);

        // 创建一个ByteArrayOutputStream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 把BufferedImage写入ByteArrayOutputStream
        ImageIO.write(outImage, "jpg", os);
        grabber.stop();
        log.info("截取视频截图结束,处理时间: {}毫秒", (System.currentTimeMillis() - timeMillis));
        // ByteArrayOutputStream转成InputStream
        return new ByteArrayInputStream(os.toByteArray());
    }
}
