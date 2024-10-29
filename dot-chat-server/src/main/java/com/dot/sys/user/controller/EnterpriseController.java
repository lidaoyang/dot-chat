package com.dot.sys.user.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.entity.ResultBean;
import com.dot.sys.user.response.EnterpriseSimResponse;
import com.dot.sys.user.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 企业管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/sys/ent")
@Tag(name = "企业管理")
public class EnterpriseController {

    @Resource
    private EnterpriseService enterpriseService;

    @ApiOperationSupport(author = "taomengmeng@dot.cn")
    @Operation(summary = "企业列表(精简)")
    @GetMapping(value = "/getSimList")
    @Parameters({@Parameter(name = "term", description = "企业名称,支持模糊搜索"),
            @Parameter(name = "industryType", description = "行业分类(1:礼品行业;2:医疗行业)")})
    public List<EnterpriseSimResponse> getSimList(@RequestParam(required = false) String term,
                                                  @RequestParam(required = false) Integer industryType) {
        return enterpriseService.getSimList(term, industryType);
    }

    @ApiOperationSupport(author = "taomengmeng@dot.cn")
    @Operation(summary = "企业列表(精简)")
    @GetMapping(value = "/sim/list")
    @Parameters({@Parameter(name = "userType", description = "用户类型"),
            @Parameter(name = "account", description = "账号")})
    public ResultBean<List<EnterpriseSimResponse>> getSimList(@RequestParam @NotBlank(message = "用户类型不能为空") String userType,
                                                              @RequestParam @NotBlank(message = "账号不能为空") String account) {
        return ResultBean.success(enterpriseService.getSimList(userType, account));
    }
}
