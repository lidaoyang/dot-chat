package com.dot.comm.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件工具
 *
 * @author: Dao-yang.
 * @date: Created in 2024/10/30 14:41
 */
@Slf4j
public class FileUtil {

    /**
     * 将输入流写入文件
     * @param inStream 文件流
     * @param targetPath 目标文件路径
     */
    public static void saveToFile(InputStream inStream, String targetPath) {
        try (OutputStream outStream = new FileOutputStream(targetPath)) {
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("文件保存失败", e);
        }
    }
}
