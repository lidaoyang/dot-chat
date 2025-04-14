package com.jr.comm.utils;

import cn.hutool.core.lang.Validator;
import com.jr.comm.em.ExceptionCodeEm;
import com.jr.comm.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        try (OutputStream outStream = new FileOutputStream(FileUtil.createFile(targetPath))) {
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("文件保存失败", e);
        }
    }

    // 对中文路径的中文部分进行编码
    public static String encodeChineseUrl(String url) {
        StringBuilder resultURL = new StringBuilder();
        // 遍历字符串
        for (int i = 0; i < url.length(); i++) {
            String charAt = String.valueOf(url.charAt(i));
            // 只对汉字处理
            if (Validator.isChinese(charAt)) {
                String encode = URLEncoder.encode(charAt, StandardCharsets.UTF_8);
                resultURL.append(encode);
            } else {
                resultURL.append(charAt);
            }
        }
        return resultURL.toString();
    }

    /**
     * 根据文件的绝对路径创建一个文件对象.
     *
     * @return 返回创建的这个文件对象
     * @author Mr.Zhang
     * @since 2020-05-08
     */
    public static File createFile(String filePath) throws IOException {
        // 获取文件的完整目录
        String fileDir = FilenameUtils.getFullPath(filePath);
        // 判断目录是否存在，不存在就创建一个目录
        File file = new File(fileDir);
        if (!file.isDirectory()) {
            // 创建失败返回null
            if (!file.mkdirs()) {
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "文件目录[" + fileDir + "]创建失败...");
            }
        }
        // 判断这个文件是否存在，不存在就创建
        file = new File(filePath);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "目标文件创建失败...");
            }
        }
        return file;
    }
}
