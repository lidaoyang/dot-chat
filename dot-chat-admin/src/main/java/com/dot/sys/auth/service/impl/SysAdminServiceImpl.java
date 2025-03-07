package com.dot.sys.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.dot.sys.auth.model.SysAdmin;
import com.dot.sys.auth.request.SysAdminAddRequest;
import com.dot.sys.auth.request.SysAdminEditRequest;
import com.dot.sys.auth.request.SysAdminSearchRequest;
import com.dot.sys.auth.response.SysAdminResponse;
import com.dot.sys.auth.service.SysAdminService;
import com.dot.sys.auth.service.SysRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员表服务接口实现
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Slf4j
@Service
public class SysAdminServiceImpl extends ServiceImpl<SysAdminDao, SysAdmin> implements SysAdminService {

    @Resource
    private TokenManager tokenManager;

    @Resource
    private SysRoleService sysRoleService;

    @Override
    public Boolean add(SysAdminAddRequest request) {
        checkAccountExist(request.getAccount());

        SysAdmin newAdmin = BeanUtil.copyProperties(request, SysAdmin.class);
        LoginUsername loginUser = tokenManager.getLoginUser();
        if (loginUser.isSuperAdmin()) { // 超级管理员 可以添加任何管理员
            return this.save(newAdmin);
        }
        checkIsExistSuperRole(request.getRoles(), loginUser);
        return this.save(newAdmin);
    }

    private void checkAccountExist(String account) {
        LambdaQueryWrapper<SysAdmin> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysAdmin::getId);
        queryWrapper.eq(SysAdmin::getAccount, account);
        if (ObjectUtil.isNotNull(this.getOne(queryWrapper))) {
            log.error("账号已被使用,account:{}", account);
            throw new ApiException(ExceptionCodeEm.VALIDATE_FAILED, "账号已被使用");
        }
    }

    @Override
    public Boolean edit(SysAdminEditRequest request) {
        SysAdmin sysAdmin = getAndCheckAdmin(request.getId());
        SysAdmin newAdmin = BeanUtil.copyProperties(request, SysAdmin.class);
        LoginUsername loginUser = tokenManager.getLoginUser();
        if (loginUser.isSuperAdmin()) { // 超级管理员 可以修改任何管理员
            return this.updateById(newAdmin);
        }
        if (!sysAdmin.getRoles().equals(request.getRoles())) {
            checkIsExistSuperRole(request.getRoles(), loginUser);
        }
        return this.updateById(newAdmin);
    }

    private SysAdmin getAndCheckAdmin(Integer id) {
        SysAdmin sysAdmin = this.getById(id);
        if (ObjectUtil.isNull(sysAdmin)) {
            log.error("管理员不存在,adminId:{}", id);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "管理员不存在");
        }
        return sysAdmin;
    }

    private void checkIsExistSuperRole(String roles, LoginUsername loginUser) {
        List<Integer> roleIds = CommUtil.stringToArrayInt(roles);
        Boolean existSuperRole = sysRoleService.isExistSuperRole(roleIds);
        if (existSuperRole) { // 普通管理员 只能添加普通管理员
            log.error("普通管理员无权操作,loginUser:{}", loginUser.mergeUsername());
            throw new ApiException(ExceptionCodeEm.FORBIDDEN, "普通管理员无权操作");
        }
    }

    @Override
    public Boolean updateStatus(Integer adminId, Boolean status) {
        SysAdmin admin = getAndCheckAdmin(adminId);
        LoginUsername loginUser = tokenManager.getLoginUser();
        if (!loginUser.isSuperAdmin()) { // 超级管理员 可以修改任何管理员
            checkIsExistSuperRole(admin.getRoles(), loginUser);
        }
        LambdaUpdateWrapper<SysAdmin> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(SysAdmin::getId, adminId);
        updateWrapper.set(SysAdmin::getStatus, status);
        return this.update(updateWrapper);
    }

    @Override
    public IPage<SysAdminResponse> getList(SysAdminSearchRequest request, PageParam pageParam) {
        LambdaQueryWrapper<SysAdmin> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ObjectUtil.isNotNull(request.getStatus()), SysAdmin::getStatus, request.getStatus());
        queryWrapper.apply(ObjectUtil.isNotNull(request.getRoleId()), CommUtil.getFindInSetSql("roles", request.getRoleId()));
        queryWrapper.and(StringUtils.isNotBlank(request.getKeywords()), wrapper -> {
            wrapper.eq(SysAdmin::getId, request.getKeywords())
                    .or().like(SysAdmin::getName, request.getKeywords())
                    .or().like(SysAdmin::getAccount, request.getKeywords());
        });
        queryWrapper.orderByDesc(SysAdmin::getLastTime).orderByDesc(SysAdmin::getId);
        Page<SysAdmin> page = this.page(Page.of(pageParam.getPage(), pageParam.getLimit()), queryWrapper);
        if (page.getRecords().isEmpty()) {
            return PageUtil.copyPage(page, new ArrayList<>());
        }
        List<Integer> roleIds = page.getRecords().stream().map(SysAdmin::getRoles).flatMap(roles -> CommUtil.stringToArrayInt(roles).stream()).distinct().toList();
        Map<Integer, String> roleNameMap = sysRoleService.getRoleNameMap(roleIds);
        List<SysAdminResponse> responseList = BeanUtil.copyToList(page.getRecords(), SysAdminResponse.class);
        responseList.forEach(response -> {
            List<Integer> roles = CommUtil.stringToArrayInt(response.getRoles());
            response.setRoleNames(roles.stream().filter(roleNameMap::containsKey).map(roleNameMap::get).collect(Collectors.joining(",")));
        });
        return PageUtil.copyPage(page, responseList);
    }

}
