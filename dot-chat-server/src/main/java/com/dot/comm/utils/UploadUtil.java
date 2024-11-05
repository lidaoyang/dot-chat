package com.dot.comm.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.sys.upload.response.UploadResponse;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 上传工具类
 */
public class UploadUtil {

    @Getter
    private static String rootPath = "";

    @Getter
    private static String type = "/image";


    @Getter
    private static String modelPath = "/public";

    @Getter
    private static String extStr = "png,jpg";

    // 文件大小上限
    @Getter
    private static int size = 2;

    // 是否压缩图片
    @Getter
    private static boolean isCompress = false;

    public static void setRootPath(String rootPath) {
        UploadUtil.rootPath = (rootPath + "/").replace(" ", "").replace("//", "/");
    }

    public static void setType(String type) {
        UploadUtil.type = type + "/";
    }


    public static void setModelPath(String modelPath) {
        UploadUtil.modelPath = modelPath + "/";
    }


    public static void setExtStr(String extStr) {
        UploadUtil.extStr = extStr;
    }


    public static void setSize(int size) {
        UploadUtil.size = size;
    }


    public static void setIsCompress(boolean isCompress) {
        UploadUtil.isCompress = isCompress;
    }


    /**
     * 判断文件扩展名是否合法
     *
     * @param extName 文件的后缀名
     * @author Mr.Zhang
     * @since 2020-05-08
     */
    public static void isContains(String extName, String extNames) {
        if (StringUtils.isNotEmpty(extNames)) {
            // 切割文件扩展名
            List<String> extensionList = CommUtil.stringToArrayStr(extNames);

            if (!extensionList.isEmpty()) {
                extName = extName.toLowerCase();
                if (!extensionList.contains(extName)) {
                    throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传文件的类型只能是：" + extNames);
                }
            } else {
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传文件的类型只能是：" + extNames);
            }
        }
    }

    /**
     * 生成文件文件名
     *
     * @param fileName 文件名
     * @author Mr.Zhang
     * @since 2020-05-08
     */
    public static String getDestPath(String fileName) {
        // 规则：  子目录/年/月/日.后缀名
        return getServerPath() + fileName;
    }

    public static String fileName(String extName) {
        return CommUtil.getUlid() + "." + extName;
    }

    public static String fileName(String fileName, String extName) {
        fileName = StrUtils.replaceSpecialChars(fileName, "x");
        return fileName + "-" + RandomUtil.randomString(12) + "." + extName;
    }


    /**
     * 生成文件在的实际的路径
     *
     * @author Mr.Zhang
     * @since 2020-05-08
     */
    public static String getServerPath() {
        // 文件分隔符转化为当前系统的格式
        return FilenameUtils.separatorsToSystem(getRootPath() + getWebPath());
    }

    /**
     * web目录可访问的路径
     *
     * @author Mr.Zhang
     * @since 2020-05-08
     */
    public static String getWebPath() {
        // 文件分隔符转化为当前系统的格式
        return getType() + getModelPath() + DateUtil.today() + "/";
    }

    /**
     * 生成文件在的实际的路径
     */
    public static String getFullPath(String rootPath, String type, String modelPath) {
        // 文件分隔符转化为当前系统的格式
        return FilenameUtils.separatorsToSystem(rootPath + getUploadPath(type, modelPath));
    }

    /**
     * web目录可访问的路径
     */
    public static String getUploadPath(String type, String modelPath) {
        // 文件分隔符转化为当前系统的格式
        return type + "/" + modelPath + "/" + DateUtil.today() + "/";
    }

    /**
     * web目录可访问的路径
     */
    public static String getUploadPath(String type, String modelPath, String rootContext) {
        // 文件分隔符转化为当前系统的格式
        return rootContext + "/" + type + "/" + modelPath + "/" + DateUtil.today() + "/";
    }

    /**
     * 检测文件大小上限
     */
    public static void checkSize(Long size, int limitSize) {
        // 文件分隔符转化为当前系统的格式
        float fileSize = (float) size / 1024 / 1024;
        String fs = String.format("%.2f", fileSize);
        if (fileSize > limitSize) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "最大允许上传" + limitSize + " MB的文件, 当前文件大小为 " + fs + " MB");
        }
    }


    /**
     * 上传文件
     *
     * @param multipartFile 上传的文件对象，必传
     * @author Mr.Zhang
     * @since 2020-05-08
     */
    private static UploadResponse saveFile(MultipartFile multipartFile) throws IOException {
        if (null == multipartFile || multipartFile.isEmpty()) {
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "上传的文件对象不存在...");
        }
        // 文件名
        String fileName = multipartFile.getOriginalFilename();
        System.out.println("fileName = " + fileName);
        // 文件后缀名
        String extName = FilenameUtils.getExtension(fileName);
        if (StringUtils.isEmpty(extName)) {
            throw new RuntimeException("文件类型未定义不能上传...");
        }

        if (fileName.length() > 99) {
            fileName = StrUtil.subPre(fileName, 90).concat(".").concat(extName);
        }

        // 文件大小验证
        checkSize(multipartFile.getSize(), getSize());

        // 判断文件的后缀名是否符合规则
        isContains(extName, getExtStr());

        // 文件名
        String newFileName = fileName(extName);
        // 创建目标文件的名称，规则请看destPath方法
        String destPath = getDestPath(newFileName);
        // 创建文件
        File file = FileUtil.createFile(destPath);
        // 保存文件
        multipartFile.transferTo(file);

        // 拼装返回的数据
        UploadResponse result = new UploadResponse();
        // 如果是图片，就进行图片处理
        if (BooleanUtils.isTrue(isCompress())) {
            //            // 图片处理
            //            String toFilePath = thumbnails(serverPath, childFile, extName);
            //            // 得到处理后的图片文件对象
            //            File file = new File(getServerPath());
            //            // 压缩后的文件后缀名
            //            String extExtName = FilenameUtils.getExtension(toFilePath);
            //            // 源文件=源文件名.压缩后的后缀名
            //            String extFileName = FilenameUtils.getBaseName(fileName) + "." + FilenameUtils.getExtension(toFilePath);
            //            result.setFileSize(file.length());
            //            result.setServerPath(toFilePath);
            //            result.setFileName(extFileName);
            //            result.setExtName(extExtName);
        } else {
            result.setFileSize(multipartFile.getSize());
            result.setFileName(fileName);
            result.setExtName(extName);
            result.setServerPath(destPath);
            result.setUrl(getWebPath() + newFileName);
            result.setType(multipartFile.getContentType());
        }
        return result;
    }

    /**
     * 上传
     *
     * @param multipartFile 上传的文件对象，必传
     * @author Mr.Zhang
     * @since 2020-05-08
     */
    public static UploadResponse file(MultipartFile multipartFile) throws IOException {
        return saveFile(multipartFile);
    }
}
