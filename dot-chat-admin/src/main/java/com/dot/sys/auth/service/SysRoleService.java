package com.dot.sys.auth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.comm.entity.PageParam;
import com.dot.sys.auth.model.SysRole;
import com.dot.sys.auth.request.SysRoleAddRequest;
import com.dot.sys.auth.request.SysRoleEditRequest;
import com.dot.sys.auth.request.SysRoleSearchRequest;
import com.dot.sys.auth.response.SysRoleInfoResponse;
import com.dot.sys.auth.response.SysRoleResponse;
import com.dot.sys.auth.response.SysRoleSimResponse;

import java.util.List;
import java.util.Map;

/**
 * 系统角色服务接口
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 添加角色
     *
     * @param request 请求对象
     * @return boolean
     */
    Boolean add(SysRoleAddRequest request);

    /**
     * 编辑角色
     *
     * @param request 请求对象
     * @return boolean
     */
    Boolean edit(SysRoleEditRequest request);

    /**
     * 更新角色状态
     *
     * @param roleId 角色ID
     * @param status 状态
     * @return boolean
     */
    Boolean updateStatus(Integer roleId, Boolean status);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return boolean
     */
    Boolean delete(Integer roleId);

    /**
     * 获取角色列表
     *
     * @param request   请求对象
     * @param pageParam 分页参数
     * @return IPage<SysRoleResponse>
     */
    IPage<SysRoleResponse> getList(SysRoleSearchRequest request, PageParam pageParam);

    /**
     * 获取角色列表
     *
     * @param keyword 关键词
     * @return List<SysRoleSimResponse>
     */
    List<SysRoleSimResponse> getSimList(String keyword);

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return SysRoleInfoResponse
     */
    SysRoleInfoResponse getInfo(Integer roleId);

    /**
     * 是否存在超级管理员角色
     *
     * @param roleIds 角色ID集合
     * @return boolean
     */
    Boolean isExistSuperRole(List<Integer> roleIds);

    /**
     * 获取角色名称Map
     *
     * @param roleIds 角色ID集合
     * @return Map<Integer, String> id -> name
     */
    Map<Integer, String> getRoleNameMap(List<Integer> roleIds);

    /**
     * 获取可用的角色ID列表，过滤掉不可用的角色ID
     *
     * @param roleIds 角色ID列表
     * @return 可用角色ID列表
     */
    List<Integer> getUsableRoleIdList(List<Integer> roleIds);

    /**
     * 获取可用的角色列表，过滤掉不可用的角色ID
     *
     * @param roleIds 角色ID列表
     * @return 可用角色ID列表
     */
    List<SysRole> getUsableRoleList(List<Integer> roleIds);

    /**
     * 检测是否有访问菜单接口取的权限
     *
     * @param menuUrl 访问URL
     * @return bool
     */
    Boolean checkUrlAccessPermissions(String menuUrl);

    /**
     * 获取最高权限角色类型
     *
     * @param roleIds 角色ID集合
     * @return Integer
     */
    Integer getMaxRoleType(List<Integer> roleIds);

}
