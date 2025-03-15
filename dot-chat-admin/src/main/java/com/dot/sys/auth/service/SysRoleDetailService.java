package com.dot.sys.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.sys.auth.dto.SysRoleMenuDto;
import com.dot.sys.auth.model.SysRoleDetail;

import java.util.Collection;
import java.util.List;

/**
 * 角色明细服务接口
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
public interface SysRoleDetailService extends IService<SysRoleDetail> {

    /**
     * 根据菜单ID更新角色明细
     *
     * @param menuId  菜单ID
     * @param menuUrl 菜单URL
     * @return true:更新成功;false:更新失败
     */
    Boolean updateMenuUrlByMenuId(Integer menuId, String menuUrl);

    /**
     * 根据菜单ID删除角色明细
     *
     * @param menuId 菜单ID
     * @return true:删除成功;false:删除失败
     */
    Boolean deleteMenuByMenuId(Integer menuId);

    /**
     * 根据角色ID删除角色明细
     *
     * @param roleId 角色ID
     * @return true:删除成功;false:删除失败
     */
    Boolean deleteDetailByMRoleId(Integer roleId);

    /**
     * 根据角色ID和菜单ID列表删除角色明细
     *
     * @param roleId     角色ID
     * @param menuIdList 菜单ID列表
     * @return true:删除成功;false:删除失败
     */
    Boolean deleteDetailByMRoleId(Integer roleId, List<Integer> menuIdList);

    /**
     * 根据角色ID列表获取菜单ID列表
     *
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    List<Integer> getMenuIdListByRoleIds(List<Integer> roleIds);

    /**
     * 根据角色ID获取菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Integer> getMenuIdsByRoleId(Integer roleId);

    /**
     * 检查权限菜单是否属于传入角色，返回true，菜单ID都属于角色,
     *
     * @param roleIds 角色ID列表
     * @param menuIds 菜单ID列表
     * @return true:菜单ID都属于传入角色;false:有部分角色不属于传入角色
     */
    Boolean checkRoleMenu(Collection<Integer> roleIds, Collection<Integer> menuIds);

    /**
     * 根据菜单URL获取角色ID列表
     *
     * @param menuUrl 菜单URL
     * @return 角色ID列表
     */
    List<Integer> getRoleIdByMenuUrl(String menuUrl);

    /**
     * 根据角色ID列表获取角色菜单列表(首页导航菜单使用)
     *
     * @param roleIds 角色ID列表
     * @return 角色菜单列表
     */
    List<SysRoleMenuDto> getSysRoleMenuList(List<Integer> roleIds);

    /**
     * 根据角色ID列表获取菜单ID列表-包含ID-pid
     *
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    List<SysRoleMenuDto> getSysMenuIdList(List<Integer> roleIds);
}
