package com.dot.sys.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.sys.auth.dao.SysRoleDetailDao;
import com.dot.sys.auth.dto.SysRoleMenuDto;
import com.dot.sys.auth.em.MenuTypeEm;
import com.dot.sys.auth.model.SysRoleDetail;
import com.dot.sys.auth.service.SysRoleDetailService;
import com.dot.sys.auth.dto.SysPermissionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色明细服务接口实现
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Slf4j
@Service
public class SysRoleDetailServiceImpl extends ServiceImpl<SysRoleDetailDao, SysRoleDetail> implements SysRoleDetailService {

    @Override
    public Boolean updateMenuUrlByMenuId(Integer menuId, String menuUrl) {
        LambdaUpdateWrapper<SysRoleDetail> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(SysRoleDetail::getMenuId, menuId)
                .set(SysRoleDetail::getMenuUrl, menuUrl);
        return this.update(updateWrapper);
    }

    @Override
    public Boolean deleteMenuByMenuId(Integer menuId) {
        LambdaQueryWrapper<SysRoleDetail> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysRoleDetail::getMenuId, menuId);
        return this.remove(queryWrapper);
    }

    @Override
    public Boolean deleteDetailByMRoleId(Integer roleId) {
        LambdaQueryWrapper<SysRoleDetail> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysRoleDetail::getRoleId, roleId);
        return this.remove(queryWrapper);
    }

    @Override
    public Boolean deleteDetailByMRoleId(Integer roleId, List<Integer> menuIdList) {
        if (CollUtil.isEmpty(menuIdList)) {
            return false;
        }
        LambdaQueryWrapper<SysRoleDetail> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysRoleDetail::getRoleId, roleId);
        queryWrapper.in(SysRoleDetail::getMenuId, menuIdList);
        return this.remove(queryWrapper);
    }

    @Override
    public List<Integer> getMenuIdListByRoleIds(List<Integer> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysRoleDetail> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysRoleDetail::getMenuId);
        queryWrapper.in(SysRoleDetail::getRoleId, roleIds);
        queryWrapper.groupBy(SysRoleDetail::getMenuId);
        return this.listObjs(queryWrapper, obj -> (Integer) obj);
    }

    @Override
    public List<Integer> getMenuIdsByRoleId(Integer roleId) {
        LambdaQueryWrapper<SysRoleDetail> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysRoleDetail::getMenuId);
        queryWrapper.eq(SysRoleDetail::getRoleId, roleId);
        List<SysRoleDetail> roleDetails = this.list(queryWrapper);
        if (CollUtil.isEmpty(roleDetails)) {
            return CollUtil.newArrayList();
        }
        return roleDetails.stream().map(SysRoleDetail::getMenuId).collect(Collectors.toList());
    }

    @Override
    public Boolean checkRoleMenu(Collection<Integer> roleIds, Collection<Integer> menuIds) {
        QueryWrapper<SysPermissionDto> qw = Wrappers.query();
        if (roleIds.size() == 1) {
            qw.eq("rd.role_id", CollUtil.get(roleIds, 0));
        } else {
            qw.in("rd.role_id", roleIds);
        }
        List<SysPermissionDto> permissionDtos = baseMapper.selectSysPermissionList(qw);
        List<Integer> menuIdList = permissionDtos.stream().map(SysPermissionDto::getId).distinct().collect(Collectors.toList());
        List<Integer> subMenuIdList = CollUtil.subtractToList(menuIds, menuIdList);
        log.warn("有{}个菜单不属于角色:{},不属于的菜单ID:{}", subMenuIdList.size(), roleIds, subMenuIdList);
        return CollUtil.isEmpty(subMenuIdList);
    }

    @Override
    public List<Integer> getRoleIdByMenuUrl(String menuUrl) {
        LambdaQueryWrapper<SysRoleDetail> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysRoleDetail::getRoleId);
        queryWrapper.eq(SysRoleDetail::getMenuUrl, menuUrl);
        queryWrapper.groupBy(SysRoleDetail::getRoleId);
        List<SysRoleDetail> list = this.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return list.stream().map(SysRoleDetail::getRoleId).collect(Collectors.toList());
    }

    @Override
    public List<SysRoleMenuDto> getSysRoleMenuList(List<Integer> roleIds) {
        QueryWrapper<SysRoleMenuDto> qw = Wrappers.query();
        if (roleIds.size() == 1) {
            qw.eq("rd.role_id", roleIds.get(0));
        } else {
            qw.in("rd.role_id", roleIds);
        }
        qw.eq("me.status", true);
        qw.in("me.type", MenuTypeEm.DIRECTORY.getCode(), MenuTypeEm.PAGE.getCode());// 只返回文件夹和页面菜单
        qw.orderByDesc("me.sort").orderByAsc("me.id");
        return baseMapper.selectSysRoleMenuList(qw);
    }
}
