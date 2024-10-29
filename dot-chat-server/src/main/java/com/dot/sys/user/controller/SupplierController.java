package com.dot.sys.user.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.dot.comm.entity.ResultBean;
import com.dot.sys.user.response.SupplierSimResponse;
import com.dot.sys.user.service.SupplierService;
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
 * 供应商管理
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/10 18:11
 */
@Validated
@RestController
@RequestMapping("api/sys/supp")
@Tag(name = "供应商管理")
public class SupplierController {

    @Resource
    private SupplierService supplierService;

    @ApiOperationSupport(author = "taomengmeng@dot.cn")
    @Operation(summary = "供应商列表(精简)")
    @GetMapping(value = "/getSimList")
    @Parameters({@Parameter(name = "term", description = "供应商名称,支持模糊搜索"),
            @Parameter(name = "industryType", description = "行业分类(1:礼品行业;2:医疗行业)")})
    public List<SupplierSimResponse> getSimList(@RequestParam(required = false) String term,
                                                @RequestParam(required = false) Integer industryType) {
        return supplierService.getSimList(term, industryType);
    }

    @ApiOperationSupport(author = "taomengmeng@dot.cn")
    @Operation(summary = "企业列表(精简)")
    @GetMapping(value = "/sim/list")
    @Parameter(name = "account", description = "账号")
    public ResultBean<List<SupplierSimResponse>> getSimList(@RequestParam @NotBlank(message = "账号不能为空") String account) {
        return ResultBean.success(supplierService.getSimList(account));
    }
}
