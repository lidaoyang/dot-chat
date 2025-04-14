package com.dot.sys.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.entity.LoginUsername;
import com.dot.comm.entity.PageParam;
import com.dot.comm.exception.ApiException;
import com.dot.comm.manager.TokenManager;
import com.dot.comm.utils.CommUtil;
import com.dot.comm.utils.PageUtil;
import com.dot.sys.auth.dao.SysAdminDao;
import com.dot.sys.auth.dao.SysMenuDao;
import com.dot.sys.auth.dao.SysRoleDao;
import com.dot.sys.auth.dto.SysRoleMenuDto;
import com.dot.sys.auth.em.RoleTypeEm;
import com.dot.sys.auth.model.SysAdmin;
import com.dot.sys.auth.model.SysMenu;
import com.dot.sys.auth.model.SysRole;
import com.dot.sys.auth.model.SysRoleDetail;
import com.dot.sys.auth.request.SysRoleAddRequest;
import com.dot.sys.auth.request.SysRoleEditRequest;
import com.dot.sys.auth.request.SysRoleSearchRequest;
import com.dot.sys.auth.request.SysRoleUpdateMenuRequest;
import com.dot.sys.auth.response.*;
import com.dot.sys.auth.service.SysRoleDetailService;
import com.dot.sys.auth.service.SysRoleService;
import com.dot.sys.auth.dto.SysMenuUrlDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统角色服务接口实现
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleDao, SysRole> implements SysRoleService {

    @Resource
    private TokenManager tokenManager;

    @Resource
    private SysAdminDao sysAdminDao;

    @Resource
    private SysMenuDao sysMenuDao;

    @Resource
    private SysRoleDetailService sysRoleDetailService;

    @Resource
    private TransactionTemplate transactionTemplate;


    @Override
    public Boolean add(SysRoleAddRequest request) {
        LoginUsername loginUser = tokenManager.getLoginUser();
        // 检查是否是超级管理员
        checkIsSuperAdmin(loginUser, request.getType());

        SysRole newSysRole = getNewSysRole(request);
        boolean ret = this.save(newSysRole);
        if (!ret) {
            log.error("角色信息保存失败,role:{}", JSON.toJSONString(newSysRole));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "角色信息保存失败");
        }
        return true;
    }

    private SysRole getNewSysRole(SysRoleAddRequest request) {
        SysRole newSysRole = new SysRole();
        newSysRole.setName(request.getName());
        newSysRole.setStatus(request.getStatus());
        newSysRole.setType(request.getType());
        return newSysRole;
    }

    @Override
    public Boolean edit(SysRoleEditRequest request) {
        LoginUsername loginUser = tokenManager.getLoginUser();
        // 检查角色是否存在和是否有操作权限
        checkLoginUserRoleAuth(request.getId(), loginUser);

        SysRole newSysRole = getNewSysRole(request);
        newSysRole.setId(request.getId());
        boolean ret = this.updateById(newSysRole);
        if (!ret) {
            log.error("角色信息更新失败,role:{}", JSON.toJSONString(newSysRole));
            throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "角色信息更新失败");
        }
        return true;
    }

    @Override
    public Boolean updateRoleMenu(SysRoleUpdateMenuRequest request) {
        LoginUsername loginUser = tokenManager.getLoginUser();
        // 旧的菜单ID列表
        List<Integer> menuIdList = sysRoleDetailService.getMenuIdsByRoleId(request.getId());

        List<Integer> menuIds = CommUtil.stringToArrayInt(request.getMenuIds());

        // 检查菜单权限
        checkLoginUserRoleAuth(menuIdList, loginUser);
        checkLoginUserRoleAuth(menuIds, loginUser);

        // 新增的菜单ID列表
        List<Integer> addMenuIdList = CollUtil.subtractToList(menuIds, menuIdList);

        // 删除的菜单ID
        List<Integer> deleteMenuIdList = CollUtil.subtractToList(menuIdList, menuIds);

        List<SysMenuUrlDto> addMenuUrlVoList = getMenuUrlListByIds(addMenuIdList);
        if (CollUtil.isEmpty(addMenuUrlVoList)) {
            log.warn("无新增的系统菜单数据,addMenuIdList:{}", addMenuIdList);
        }
        return transactionTemplate.execute(t -> {
            Boolean ret1 = sysRoleDetailService.deleteDetailByMRoleId(request.getId(), deleteMenuIdList);
            if (!ret1) {
                log.warn("没有权限菜单数据被删除,roleId:{},deleteMenuIdList:{}", request.getId(), deleteMenuIdList);
            }
            if (CollUtil.isNotEmpty(addMenuUrlVoList)) {
                List<SysRoleDetail> roleDetails = getRoleDetails(addMenuUrlVoList, request.getId());
                boolean rt = sysRoleDetailService.saveBatch(roleDetails);
                if (!rt) {
                    log.error("角色明细保存失败,roleDetails:{}", JSON.toJSONString(roleDetails));
                    throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "角色明细保存失败");
                }
            }
            return true;
        });
    }

    private void checkLoginUserRoleAuth(Collection<Integer> menuIdList, LoginUsername loginUser) {
        if (loginUser.isSuperAdmin()) {// 超级管理员拥有所有权限
            return;
        }
        // 获取可用角色ID列表
        List<Integer> roleIdList = getUsableRoleIdList(loginUser.getRoleList());
        if (CollUtil.isEmpty(roleIdList)) {
            log.error("角色不存在或被禁用,roleIds:{}", loginUser.getRoles());
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "角色不存在或被禁用，请联系管理员");
        }
        Boolean checked = sysRoleDetailService.checkRoleMenu(roleIdList, menuIdList);
        if (!checked) {
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "部分菜单你没有权限操作");
        }
    }

    private List<SysMenuUrlDto> getMenuUrlListByIds(Collection<Integer> menuIdList) {
        if (CollUtil.isEmpty(menuIdList)) {
            return CollUtil.newArrayList();
        }
        LambdaQueryWrapper<SysMenu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysMenu::getId, SysMenu::getLinkUrl);
        queryWrapper.in(SysMenu::getId, menuIdList);
        List<SysMenu> menuList = sysMenuDao.selectList(queryWrapper);
        if (CollUtil.isEmpty(menuList)) {
            return CollUtil.newArrayList();
        }
        return BeanUtil.copyToList(menuList, SysMenuUrlDto.class);
    }

    private List<SysRoleDetail> getRoleDetails(List<SysMenuUrlDto> menuUrlVoList, Integer roleId) {
        List<SysRoleDetail> roleDetails = new ArrayList<>();
        menuUrlVoList.forEach(menuUrlVo -> {
            SysRoleDetail roleDetail = new SysRoleDetail();
            roleDetail.setRoleId(roleId);
            roleDetail.setMenuId(menuUrlVo.getId());
            roleDetail.setMenuUrl(menuUrlVo.getLinkUrl());
            roleDetails.add(roleDetail);
        });
        return roleDetails;
    }

    @Override
    public Boolean updateStatus(Integer roleId, Boolean status) {
        LoginUsername loginUser = tokenManager.getLoginUser();
        // 检查角色是否存在和是否有操作权限
        checkLoginUserRoleAuth(roleId, loginUser);
        // 获取菜单ID列表
        List<Integer> menuIdList = sysRoleDetailService.getMenuIdsByRoleId(roleId);
        // 检查菜单权限
        checkLoginUserRoleAuth(menuIdList, loginUser);
        // 更新状态
        LambdaUpdateWrapper<SysRole> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(SysRole::getId, roleId);
        updateWrapper.set(SysRole::getStatus, status);
        return this.update(updateWrapper);
    }

    @Override
    public Boolean delete(Integer roleId) {
        // 检查当前用户是否有权操作这个角色ID
        checkLoginUserRoleAuth(roleId);
        Boolean checked = checkRoleUsedInAdmin(roleId);
        if (checked) {
            log.error("当前角色正在被管理员使用，不能删除,roleId:{}", roleId);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "当前角色正在被管理员使用，不能删除");
        }
        return transactionTemplate.execute(t -> {
            boolean ret = this.removeById(roleId);
            if (!ret) {
                log.error("删除角色失败,roleId:{}", roleId);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "删除角色失败");
            }
            Boolean ret2 = sysRoleDetailService.deleteDetailByMRoleId(roleId);
            if (!ret2) {
                log.error("删除角色明细失败,roleId:{}", roleId);
                throw new ApiException(ExceptionCodeEm.SYSTEM_ERROR, "删除角色失败");
            }
            return true;
        });
    }

    /**
     * 检查角色是否被使用
     *
     * @param roleId 角色id
     * @return Boolean
     */
    private Boolean checkRoleUsedInAdmin(Integer roleId) {
        LambdaQueryWrapper<SysAdmin> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysAdmin::getId);
        queryWrapper.apply(CommUtil.getFindInSetSql("roles", roleId));
        queryWrapper.last("limit 1");
        return ObjectUtil.isNotNull(sysAdminDao.selectOne(queryWrapper));
    }


    private void checkLoginUserRoleAuth(Integer roleId) {
        LoginUsername loginUser = tokenManager.getLoginUser();
        checkLoginUserRoleAuth(roleId, loginUser);
    }

    private void checkLoginUserRoleAuth(Integer roleId, LoginUsername loginUser) {
        SysRole role = checkRoleExist(roleId);
        checkIsSuperAdmin(loginUser, role.getType());

    }

    private void checkIsSuperAdmin(LoginUsername loginUser, Integer roleType) {
        if (!loginUser.isSuperAdmin() && roleType == RoleTypeEm.SUPER_ADMIN.getCode()) {
            log.error("当前用户不是超级管理员无权限操作,username:{},roleType:{}", loginUser.mergeUsername(), roleType);
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "当前角色你无权限操作");
        }
    }

    private SysRole checkRoleExist(Integer roleId) {
        SysRole role = this.getById(roleId);
        if (ObjectUtil.isNull(role)) {
            log.error("未查询到角色信息,roleId:{}", roleId);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "未查询到角色信息");
        }
        return role;
    }

    @Override
    public IPage<SysRoleResponse> getList(SysRoleSearchRequest request, PageParam pageParam) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(request.getType()), SysRole::getType, request.getType());
        queryWrapper.eq(ObjectUtil.isNotNull(request.getStatus()), SysRole::getStatus, request.getStatus());
        queryWrapper.and(StringUtils.isNotBlank(request.getKeywords()), wrapper -> {
            wrapper.eq(SysRole::getId, request.getKeywords())
                    .or()
                    .like(SysRole::getName, request.getKeywords());
        });
        queryWrapper.orderByAsc(SysRole::getId);
        Page<SysRole> page = this.page(Page.of(pageParam.getPageIndex(), pageParam.getPageSize()), queryWrapper);
        List<SysRoleResponse> responseList = BeanUtil.copyToList(page.getRecords(), SysRoleResponse.class);
        return PageUtil.copyPage(page, responseList);
    }

    @Override
    public List<SysRoleSimResponse> getSimList(String keyword) {
        LambdaQueryWrapper<SysRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysRole::getId, SysRole::getName);
        queryWrapper.eq(SysRole::getStatus, true);
        queryWrapper.and(StringUtils.isNotBlank(keyword), wrapper ->
                wrapper.eq(SysRole::getId, keyword)
                        .or()
                        .like(SysRole::getName, keyword));
        List<SysRole> roleList = this.list(queryWrapper);
        if (CollUtil.isEmpty(roleList)) {
            return Collections.emptyList();
        }
        return BeanUtil.copyToList(roleList, SysRoleSimResponse.class);
    }

    @Override
    public SysRoleInfoResponse getInfo(Integer roleId) {
        SysRole role = checkRoleExist(roleId);
        SysRoleInfoResponse response = new SysRoleInfoResponse();
        BeanUtil.copyProperties(role, response);
        response.setMenuIds(sysRoleDetailService.getMenuIdsByRoleId(roleId));
        return response;
    }

    @Override
    public String getRoleMenuIds(Integer roleId) {
        List<SysRoleMenuDto> menuList = sysRoleDetailService.getSysMenuIdList(CollUtil.newArrayList(roleId));
        Map<Integer, SysMenuSimResponse> menuMap = new HashMap<>();
        menuList.forEach(menu -> {
            SysMenuSimResponse menuResponse = new SysMenuSimResponse();
            menuResponse.setId(menu.getId());
            menuResponse.setPid(menu.getPid());
            menuMap.put(menu.getId(), menuResponse);
        });
        menuMap.forEach((menuId, menu) -> {
            if (menu.getPid() != 0 && menuMap.containsKey(menu.getPid())) {
                SysMenuSimResponse menuResponse = menuMap.get(menu.getPid());
                menuResponse.getChildren().add(menu);
            }
        });

        List<Integer> menuIds = new ArrayList<>();
        menuMap.forEach((menuId, menu) -> {
            if (menu.getChildren().isEmpty()) {
                menuIds.add(menuId);
            }
        });
        return CollUtil.join(menuIds, ",");
    }

    @Override
    public Boolean isExistSuperRole(List<Integer> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }
        LambdaQueryWrapper<SysRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysRole::getId);
        queryWrapper.eq(SysRole::getType, RoleTypeEm.SUPER_ADMIN.getCode());
        queryWrapper.in(SysRole::getId, roleIds);
        queryWrapper.last("limit 1");
        return ObjectUtil.isNotNull(this.getOne(queryWrapper));
    }

    @Override
    public Map<Integer, String> getRoleNameMap(List<Integer> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<SysRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysRole::getId, SysRole::getName);
        queryWrapper.eq(SysRole::getStatus, true);
        queryWrapper.in(SysRole::getId, roleIds);
        List<SysRole> roleList = this.list(queryWrapper);
        return roleList.stream().collect(Collectors.toMap(SysRole::getId, SysRole::getName));
    }

    @Override
    public List<Integer> getUsableRoleIdList(List<Integer> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysRole::getId);
        queryWrapper.in(SysRole::getId, roleIds);
        queryWrapper.eq(SysRole::getStatus, true);// 只查询启用的角色
        List<SysRole> roleList = this.list(queryWrapper);
        return roleList.stream().map(SysRole::getId).collect(Collectors.toList());
    }

    @Override
    public List<SysRole> getUsableRoleList(List<Integer> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysRole::getId, SysRole::getType);
        queryWrapper.in(SysRole::getId, roleIds);
        queryWrapper.eq(SysRole::getStatus, true);// 只查询启用的角色
        return this.list(queryWrapper);
    }

    @Override
    public Boolean checkUrlAccessPermissions(String menuUrl) {
        LoginUsername loginUser = tokenManager.getLoginUser();
        List<Integer> roleIds = sysRoleDetailService.getRoleIdByMenuUrl(menuUrl);
        List<Integer> roleList = loginUser.getRoleList();
        for (Integer rid : roleIds) {
            Optional<Integer> first = roleList.stream().filter(rid::equals).findFirst();
            if (first.isPresent()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer getMaxRoleType(List<Integer> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return null;
        }
        LambdaQueryWrapper<SysRole> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysRole::getId, SysRole::getType);
        queryWrapper.in(SysRole::getId, roleIds);
        queryWrapper.eq(SysRole::getStatus, true);//
        queryWrapper.orderByAsc(SysRole::getType);
        queryWrapper.last("limit 1");
        SysRole role = this.getOne(queryWrapper);
        if (ObjectUtil.isNotNull(role)) {
            return role.getType();
        }
        return null;
    }

    @Override
    public List<SysRoleMenuResponse> getRoleMenuList() {
        LoginUsername loginUser = tokenManager.getLoginUser();
        List<SysRoleMenuDto> roleMenuDtoList = sysRoleDetailService.getSysRoleMenuList(loginUser.getRoleList());
        Map<Integer, SysRoleMenuResponse> menuMap = new LinkedHashMap<>();
        roleMenuDtoList.forEach(menu -> {
            SysRoleMenuResponse menuResponse = new SysRoleMenuResponse();
            BeanUtils.copyProperties(menu, menuResponse);
            menuMap.put(menu.getId(), menuResponse);
        });
        List<SysRoleMenuResponse> responseList = getMenuTreeResponseList(menuMap);
        CollUtil.sortByProperty(responseList, "sort");
        CollUtil.reverse(responseList);
        return responseList;
    }

    private List<SysRoleMenuResponse> getMenuTreeResponseList(Map<Integer, SysRoleMenuResponse> menuMap) {
        List<SysRoleMenuResponse> responseList = new ArrayList<>();
        menuMap.forEach((menuId, menu) -> {
            if (menu.getPid() != 0 && menuMap.containsKey(menu.getPid())) {
                SysRoleMenuResponse menuResponse = menuMap.get(menu.getPid());
                menuResponse.getChildren().add(menu);
            } else {
                responseList.add(menu);
            }
        });
        return responseList;
    }
}
