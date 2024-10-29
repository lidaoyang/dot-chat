package com.dot.comm.entity;

import com.dot.comm.constants.CommConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页公共请求对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParamRequest {

    private int page = CommConstant.DEFAULT_PAGE;

    private int limit = CommConstant.DEFAULT_LIMIT;

}
