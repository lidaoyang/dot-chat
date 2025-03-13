package com.dot.sys.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
import com.dot.comm.utils.CommUtil;
import com.dot.sys.auth.dao.SysMenuDao;
import com.dot.sys.auth.em.MenuTypeEm;
import com.dot.sys.auth.model.SysMenu;
import com.dot.sys.auth.request.SysMenuAddRequest;
import com.dot.sys.auth.request.SysMenuEditRequest;
import com.dot.sys.auth.request.SysMenuSearchRequest;
import com.dot.sys.auth.response.SysMenuResponse;
import com.dot.sys.auth.response.SysMenuSimResponse;
import com.dot.sys.auth.service.SysMenuService;
import com.dot.sys.auth.service.SysRoleDetailService;
import com.dot.sys.auth.service.SysRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

/**
 * 系统菜单服务接口实现
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Slf4j
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuDao, SysMenu> implements SysMenuService {

    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysRoleDetailService sysRoleDetailService;

    @Resource
    private TokenManager tokenManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Boolean add(SysMenuAddRequest request) {
        SysMenu newMenu = getNewSysMenu(request);
        return this.save(newMenu);
    }

    private SysMenu getNewSysMenu(SysMenuAddRequest addRequest) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(addRequest, menu);
        if (menu.getPid() == null || menu.getPid() == 0) {
            menu.setLevel(1);
            menu.setPath("");
        } else {
            menu.setPath(getPathById(menu.getPid()));
            menu.setLevel(1 + menu.getPath().split(",").length);
        }
        return menu;
    }

    @Override
    public Boolean edit(SysMenuEditRequest request) {
        SysMenu oldMenu = getSysMenu(request.getId());
        if (request.getPid() == null) {
            request.setPid(0);
        }
        SysMenu newMenu = getNewSysMenu(request);
        newMenu.setId(oldMenu.getId());

        // 修改了父级ID，需要同时修改他的子集的父级ID
        List<SysMenu> subMenuList = getSubMenuList(newMenu, oldMenu);
        return transactionTemplate.execute(t -> {
            updateMenu(newMenu);
            if (CollUtil.isNotEmpty(subMenuList)) {
                log.info("修改菜单子集path");
                updateChildMenu(subMenuList);
            }
            updateRoleDetailMenuUrl(request);
            return true;
        });
    }

    private SysMenu getSysMenu(Integer menuId) {
        SysMenu oldMenu = this.getById(menuId);
        if (ObjectUtil.isNull(oldMenu)) {
            log.error("菜单不存在,id:{}", menuId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "菜单不存在");
        }
        return oldMenu;
    }

    private List<SysMenu> getSubMenuList(SysMenu menu, SysMenu oldMenu) {
        // 修改了父级ID，需要同时修改他的子集的父级ID
        if (!Objects.equals(oldMenu.getPid(), menu.getPid())) {
            List<SysMenu> subMenuList = getSysMenusByPath(menu.getId());
            if (CollUtil.isNotEmpty(subMenuList)) {
                List<Integer> oldPIds = CommUtil.stringToArrayInt(oldMenu.getPath());
                List<Integer> newPIds = CommUtil.stringToArrayInt(menu.getPath());
                subMenuList.forEach(me -> {
                    String menuIds = getMenuIds(oldPIds, newPIds, me);
                    me.setPath(menuIds);
                });
            }
        }
        return Collections.emptyList();
    }


    private String getMenuIds(List<Integer> oldPIds, List<Integer> newPIds, SysMenu me) {
        List<Integer> pids = CommUtil.stringToArrayInt(me.getPath());
        List<Integer> subPids = CollUtil.subtractToList(pids, oldPIds);
        if (CollUtil.isNotEmpty(subPids)) {
            subPids.addAll(0, newPIds);
        } else {
            subPids = newPIds;
        }
        return CollUtil.join(subPids, ",");
    }

    private List<SysMenu> getSysMenusByPath(Integer menId) {
        LambdaQueryWrapper<SysMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysMenu::getId, SysMenu::getPath);
        queryWrapper.apply(CommUtil.getFindInSetSql("path", menId));
        return this.list(queryWrapper);
    }

    private void updateMenu(SysMenu newMenu) {
        boolean ret = this.updateById(newMenu);
        if (!ret) {
            log.error("菜单更新失败,menu:{}", JSON.toJSONString(newMenu));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "菜单更新失败");
        }
    }

    private void updateChildMenu(List<SysMenu> subMenuList) {
        boolean rt = this.updateBatchById(subMenuList);
        if (!rt) {
            log.error("菜单子集更新失败,subMenuList:{}", JSON.toJSONString(subMenuList));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "菜单子集更新失败");
        }
    }

    private void updateRoleDetailMenuUrl(SysMenuEditRequest request) {
        Boolean rt = sysRoleDetailService.updateMenuUrlByMenuId(request.getId(), request.getLinkUrl());
        if (!rt) {
            log.warn("未更新角色明细菜单URL,menuId:{},url:{}", request.getId(), request.getLinkUrl());
        }
    }

    @Override
    public Boolean delete(Integer menuId) {
        boolean removed = this.removeById(menuId);
        if (removed) {
            Boolean rt = sysRoleDetailService.deleteMenuByMenuId(menuId);
            if (!rt) {
                log.warn("未删除角色明细中菜单,menuId:{}", menuId);
            }
        }

        return removed;
    }

    private String getPathById(Integer id) {
        LambdaQueryWrapper<SysMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysMenu::getPid, SysMenu::getPath);
        queryWrapper.eq(SysMenu::getId, id);
        SysMenu one = this.getOne(queryWrapper);
        if (one == null) {
            return null;
        }
        if (one.getPid() == 0) {
            return id.toString();
        }
        return one.getPath() + "," + id;
    }

    @Override
    public Boolean updateStatus(Integer menuId, Boolean status, Boolean updateChild) {
        LambdaUpdateWrapper<SysMenu> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(SysMenu::getId, menuId);
        updateWrapper.set(SysMenu::getStatus, status);
        boolean ret = this.update(updateWrapper);
        if (ret && updateChild) { // 更新子集菜单状态
            LambdaUpdateWrapper<SysMenu> updateChildWrapper = Wrappers.lambdaUpdate();
            updateChildWrapper.apply(CommUtil.getFindInSetSql("path", menuId));
            updateChildWrapper.set(SysMenu::getStatus, status);
            boolean rt = this.update(updateChildWrapper);
            log.info("子集菜单更新完成,pid:{},ret:{}", menuId, rt);
        }
        return ret;
    }

    @Override
    public Boolean existApiUrl(String apiUrl) {
        LambdaQueryWrapper<SysMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysMenu::getId);
        queryWrapper.eq(SysMenu::getType, MenuTypeEm.API.getCode());
        queryWrapper.eq(SysMenu::getLinkUrl, apiUrl);
        queryWrapper.last("limit 1");
        return ObjectUtil.isNotNull(getOne(queryWrapper));
    }

    @Override
    public List<SysMenuResponse> getList(SysMenuSearchRequest request) {
        List<SysMenu> menuList = getSysMenuList(request);
        if (CollUtil.isEmpty(menuList)) {
            return CollUtil.newArrayList();
        }
        Map<Integer, SysMenuResponse> menuMap = new LinkedHashMap<>();
        menuList.forEach(menu -> {
            SysMenuResponse menuResponse = new SysMenuResponse();
            BeanUtils.copyProperties(menu, menuResponse);
            menuMap.put(menu.getId(), menuResponse);
        });
        List<SysMenuResponse> responseList = new ArrayList<>();
        menuMap.forEach((menuId, menu) -> {
            if (menu.getPid() != 0 && menuMap.containsKey(menu.getPid())) {
                SysMenuResponse menuResponse = menuMap.get(menu.getPid());
                menuResponse.getChildren().add(menu);
            } else {
                responseList.add(menu);
            }
        });
        CollUtil.sortByProperty(responseList, "sort");
        CollUtil.reverse(responseList);
        return responseList;
    }

    @Override
    public List<SysMenuSimResponse> getSimList(SysMenuSearchRequest request) {
        List<SysMenu> menuList = getSysMenuList(request);
        if (CollUtil.isEmpty(menuList)) {
            return CollUtil.newArrayList();
        }
        return getSysMenuSimResponseList(menuList);
    }

    private List<SysMenuSimResponse> getSysMenuSimResponseList(List<SysMenu> menuList) {
        Map<Integer, SysMenuSimResponse> menuMap = new LinkedHashMap<>();
        menuList.forEach(menu -> {
            SysMenuSimResponse menuResponse = new SysMenuSimResponse();
            BeanUtils.copyProperties(menu, menuResponse);
            menuMap.put(menu.getId(), menuResponse);
        });
        List<SysMenuSimResponse> responseList = new ArrayList<>();
        menuMap.forEach((menuId, menu) -> {
            if (menu.getPid() != 0 && menuMap.containsKey(menu.getPid())) {
                SysMenuSimResponse menuResponse = menuMap.get(menu.getPid());
                menuResponse.getChildren().add(menu);
            } else {
                responseList.add(menu);
            }
        });
        CollUtil.sortByProperty(responseList, "sort");
        CollUtil.reverse(responseList);
        return responseList;
    }

    @Override
    public List<SysMenuSimResponse> getDirSimList(SysMenuSearchRequest request) {
        request.setTypes(CollUtil.newArrayList(MenuTypeEm.DIRECTORY.getCode(), MenuTypeEm.PAGE.getCode()));
        List<SysMenu> menuList = getSysMenuList(request);
        if (CollUtil.isEmpty(menuList)) {
            return CollUtil.newArrayList();
        }
        return getSysMenuSimResponseList(menuList);
    }

    private List<SysMenu> getSysMenuList(SysMenuSearchRequest request) {
        LoginUsername loginUser = tokenManager.getLoginUser();
        // 当登录用户非超级管理,获取当前用户的菜单ID集合
        List<Integer> menuIdList = new ArrayList<>();
        if (!loginUser.isSuperAdmin()) { // 当登录用户非超级管理,获取当前用户的菜单ID集合
            // 获取可用角色ID列表
            List<Integer> roleIdList = sysRoleService.getUsableRoleIdList(loginUser.getRoleList());
            if (CollUtil.isEmpty(roleIdList)) {
                log.warn("没有可以角色,loginUser:{}", loginUser.mergeUsername());
                return CollUtil.newArrayList();
            }
            menuIdList = sysRoleDetailService.getMenuIdListByRoleIds(roleIdList);
            if (CollUtil.isEmpty(menuIdList)) {
                log.warn("没有可用的菜单,loginUser:{}", loginUser.mergeUsername());
                return CollUtil.newArrayList();
            }
        }

        LambdaQueryWrapper<SysMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ObjectUtil.isNotNull(request.getStatus()), SysMenu::getStatus, request.getStatus());
        queryWrapper.eq(ObjectUtil.isNotNull(request.getType()), SysMenu::getType, request.getType());
        queryWrapper.eq(ObjectUtil.isNotNull(request.getLevel()), SysMenu::getLevel, request.getLevel());
        queryWrapper.in(CollUtil.isNotEmpty(request.getTypes()), SysMenu::getType, request.getTypes());
        queryWrapper.in(CollUtil.isNotEmpty(menuIdList), SysMenu::getId, menuIdList);
        queryWrapper.and(StringUtils.isNotBlank(request.getKeywords()), wrapper -> {
            wrapper.eq(SysMenu::getId, request.getKeywords()).or().like(SysMenu::getName, request.getKeywords()).or()
                    .like(SysMenu::getLinkUrl, request.getKeywords());
        });
        queryWrapper.orderByDesc(SysMenu::getSort).orderByAsc(SysMenu::getId);
        return this.list(queryWrapper);
    }



}
