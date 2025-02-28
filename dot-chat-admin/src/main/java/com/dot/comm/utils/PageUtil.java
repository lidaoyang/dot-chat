package com.dot.comm.utils;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 分页工具类
 *
 * @author: Dao-yang.
 * @date: Created in 2024/1/24 20:25
 */
public class PageUtil {

    public static <T> IPage<T> copyPage(IPage<?> originPage, List<T> list) {
        Page<T> newPage = new Page<>();
        BeanUtil.copyProperties(originPage, newPage);
        newPage.setRecords(list);
        return newPage;
    }
}
