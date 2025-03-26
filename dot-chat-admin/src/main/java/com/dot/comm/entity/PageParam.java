package com.dot.comm.entity;

import com.dot.comm.constants.CommConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 分页参数对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "PageParam", description = "分页参数对象")
public class PageParam {

    @Schema(description = "当前页码,前端从0开始", defaultValue = "0")
    private int pageIndex = CommConstant.DEFAULT_PAGE;

    /**
     * 获取当前页码,查询数据时,页码从1开始
     *
     * @return 当前页码
     */
    public int getPageIndex() {
        return pageIndex + 1;
    }

    @Schema(description = "每页数量", defaultValue = "20")
    private int pageSize = CommConstant.DEFAULT_LIMIT;

}
