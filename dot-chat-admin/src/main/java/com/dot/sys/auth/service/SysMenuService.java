package com.dot.sys.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.sys.auth.model.SysMenu;
import com.dot.sys.auth.request.SysMenuAddRequest;
import com.dot.sys.auth.request.SysMenuEditRequest;
import com.dot.sys.auth.request.SysMenuSearchRequest;
import com.dot.sys.auth.response.SysMenuResponse;
import com.dot.sys.auth.response.SysMenuSimResponse;

import java.util.List;

/**
 * 系统菜单服务接口
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 添加系统菜单
     *
     * @param request 请求参数
     * @return boolean
     */
    Boolean add(SysMenuAddRequest request);

    /**
     * 编辑系统菜单
     *
     * @param request 请求参数
     * @return boolean
     */
    Boolean edit(SysMenuEditRequest request);

    /**
     * 删除系统菜单
     *
     * @param menuId 菜单ID
     * @return boolean
     */
    Boolean delete(Integer menuId);

    /**
     * 更新系统菜单状态
     *
     * @param menuId      菜单ID
     * @param status      状态
     * @param updateChild 是否更新子菜单
     * @return boolean
     */
    Boolean updateStatus(Integer menuId, Boolean status, Boolean updateChild);

    /**
     * 判断API接口是否存在
     *
     * @param apiUrl API接口
     * @return boolean
     */
    Boolean existApiUrl(String apiUrl);

    /**
     * 获取系统菜单列表
     *
     * @param request 请求参数
     * @return List<SysMenuResponse>
     */
    List<SysMenuResponse> getList(SysMenuSearchRequest request);

    /**
     * 获取系统菜单列表(用户角色选择菜单)
     *
     * @param request 请求参数
     * @return List<SysMenuSimResponse>
     */
    List<SysMenuSimResponse> getSimList(SysMenuSearchRequest request);

    /**
     * 获取系统菜单目录列表(用户创建菜单选择父级)
     *
     * @param request 请求参数
     * @return List<SysMenuSimResponse>
     */
    List<SysMenuSimResponse> getDirSimList(SysMenuSearchRequest request);
}
