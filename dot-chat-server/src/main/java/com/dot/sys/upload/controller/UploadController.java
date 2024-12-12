package com.dot.sys.upload.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.entity.ResultBean;
import com.dot.sys.upload.response.UploadProgressBarResponse;
import com.dot.sys.upload.response.UploadResponse;
import com.dot.sys.upload.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户接口
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/sys/upload")
@Tag(name = "上传管理")
public class UploadController {

    @Resource
    private UploadService uploadService;

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "单个图片文件上传(同步上传)")
    @PostMapping("/image")
    @Parameters({
            @Parameter(name = "file", description = "图片文件", in = ParameterIn.DEFAULT, required = true,
                    schema = @Schema(name = "file", format = "binary")),
            @Parameter(name = "model", description = "模块 小程序miniapp,用户user,商品product,商品详情content,微信wechat,news文章", required = true)
    })
    public ResultBean<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("model") @NotBlank(message = "模块不能为空") String model) {
        return ResultBean.success(uploadService.uploadImage(file, model));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "单个图片文件上传(异步上传)")
    @PostMapping("/async/image")
    @Parameters({
            @Parameter(name = "file", description = "图片文件", in = ParameterIn.DEFAULT, required = true,
                    schema = @Schema(name = "file", format = "binary")),
            @Parameter(name = "model", description = "模块 小程序miniapp,用户user,商品product,商品详情content,微信wechat,news文章", required = true)
    })
    public ResultBean<UploadResponse> uploadImageAsync(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("model") @NotBlank(message = "模块不能为空") String model) {
        return ResultBean.success(uploadService.uploadImageAsync(file, model));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "单个图片文件上传(同步上传)", description = "返回文件服务器地址")
    @PostMapping("/img")
    @Parameter(name = "file", description = "图片文件", in = ParameterIn.DEFAULT, required = true,
            schema = @Schema(name = "file", format = "binary"))
    public String uploadImageToStr(@RequestParam("file") MultipartFile file) {
        return uploadService.uploadImage(file, "message").getUploadPath();
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "单个文件上传(同步上传)")
    @PostMapping("/file")
    @Parameters({
            @Parameter(name = "file", description = "文件", in = ParameterIn.DEFAULT, required = true,
                    schema = @Schema(name = "file", format = "binary")),
            @Parameter(name = "model", description = "模块 小程序miniapp,用户user,商品product,商品详情content,微信wechat,news文章", required = true)
    })
    public ResultBean<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("model") @NotBlank(message = "模块不能为空") String model) {
        return ResultBean.success(uploadService.uploadFile(file, model));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "单个文件上传(异步上传)")
    @PostMapping("/async/file")
    @Parameters({
            @Parameter(name = "file", description = "文件", in = ParameterIn.DEFAULT, required = true,
                    schema = @Schema(name = "file", format = "binary")),
            @Parameter(name = "model", description = "模块 小程序miniapp,用户user,商品product,商品详情content,微信wechat,news文章", required = true)
    })
    public ResultBean<UploadResponse> uploadFileAsync(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("model") @NotBlank(message = "模块不能为空") String model) {
        return ResultBean.success(uploadService.uploadFileAsync(file, model));
    }

    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "上传视频文件(异步上传)")
    @PostMapping("/async/video")
    @Parameters({
            @Parameter(name = "file", description = "文件", in = ParameterIn.DEFAULT, required = true,
                    schema = @Schema(name = "file", format = "binary")),
            @Parameter(name = "model", description = "模块 小程序miniapp,用户user,商品product,商品详情content,微信wechat,news文章", required = true)
    })
    public ResultBean<UploadResponse> uploadVideoAsync(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("model") @NotBlank(message = "模块不能为空") String model) {
        return ResultBean.success(uploadService.uploadVideoAsync(file, model));
    }

    /**
     * 上传文件进度条
     */
    @ApiOperationSupport(author = "daoyang@dot.cn")
    @Operation(summary = "上传文件进度条")
    @GetMapping(value = "/progressBar")
    @Parameter(name = "fileName", description = "文件名称(上传后产生新文件名称)", required = true)
    public ResultBean<UploadProgressBarResponse> getUploadProgressBar(@RequestParam("fileName") String fileName) {
        return ResultBean.success(uploadService.getUploadProgressBar(fileName));
    }
}
