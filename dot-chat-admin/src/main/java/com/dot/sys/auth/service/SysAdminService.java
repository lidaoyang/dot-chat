package com.dot.sys.auth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.comm.entity.PageParam;
import com.dot.sys.auth.model.SysAdmin;
import com.dot.sys.auth.request.SysAdminAddRequest;
import com.dot.sys.auth.request.SysAdminEditRequest;
import com.dot.sys.auth.request.SysAdminSearchRequest;
import com.dot.sys.auth.response.SysAdminResponse;

/**
 * 管理员表服务接口
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
public interface SysAdminService extends IService<SysAdmin> {

    /**
     * 添加管理员
     *
     * @param request 请求参数
     * @return Boolean
     */
    Boolean add(SysAdminAddRequest request);

    /**
     * 编辑管理员
     *
     * @param request 请求参数
     * @return Boolean
     */
    Boolean edit(SysAdminEditRequest request);

    /**
     * 更新管理员状态
     *
     * @param adminId 管理员id
     * @param status  状态
     * @return Boolean
     */
    Boolean updateStatus(Integer adminId, Boolean status);

    /**
     * 获取管理员分页列表
     *
     * @param request   请求参数
     * @param pageParam 分页参数
     * @return IPage<SysAdminResponse>
     */
    IPage<SysAdminResponse> getList(SysAdminSearchRequest request, PageParam pageParam);



}
