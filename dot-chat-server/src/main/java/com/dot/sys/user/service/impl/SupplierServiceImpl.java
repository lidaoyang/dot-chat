package com.dot.sys.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.em.UserTypeEm;
import com.dot.sys.user.dao.SupplierDao;
import com.dot.sys.user.model.Supplier;
import com.dot.sys.user.response.SupplierSimResponse;
import com.dot.sys.user.service.SupplierService;
import com.dot.sys.user.service.SystemAdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
@Slf4j
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierDao, Supplier> implements SupplierService {

    @Resource
    private SystemAdminService systemAdminService;

    @Override
    public List<SupplierSimResponse> getSimList(String name, Integer industryType) {
        LambdaQueryWrapper<Supplier> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(Supplier::getId, Supplier::getIndustryType, Supplier::getName);
        queryWrapper.eq(Supplier::getStatus, 2);
        queryWrapper.eq(ObjectUtil.isNotNull(industryType), Supplier::getIndustryType, industryType);
        queryWrapper.like(StringUtils.isNotBlank(name), Supplier::getName, name);
        queryWrapper.orderByDesc(Supplier::getId);
        List<Supplier> supplierList = this.list(queryWrapper);
        List<SupplierSimResponse> responseList = new ArrayList<>();
        if (CollUtil.isEmpty(supplierList)) {
            return responseList;
        }
        supplierList.forEach(supplier -> {
            SupplierSimResponse response = new SupplierSimResponse();
            response.setEnterpriseId(supplier.getId());
            response.setName(supplier.getName());
            response.setIndustryType(supplier.getIndustryType());

            responseList.add(response);
        });
        return responseList;
    }

    @Override
    public List<SupplierSimResponse> getSimList(String phone) {
        List<Integer> enterpriseIdList = systemAdminService.getEnterpriseIdList(UserTypeEm.SUPPLIER.getCode(), phone);
        if (CollUtil.isEmpty(enterpriseIdList)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Supplier> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(Supplier::getId, Supplier::getName);
        queryWrapper.in(Supplier::getId, enterpriseIdList);
        queryWrapper.eq(Supplier::getStatus, 2);
        queryWrapper.orderByAsc(Supplier::getId);
        List<Supplier> supplierList = this.list(queryWrapper);
        List<SupplierSimResponse> responseList = new ArrayList<>();
        if (CollUtil.isEmpty(supplierList)) {
            return responseList;
        }
        supplierList.forEach(supplier -> {
            SupplierSimResponse response = new SupplierSimResponse();
            response.setEnterpriseId(supplier.getId());
            response.setName(supplier.getName());
            responseList.add(response);
        });
        return responseList;
    }

    @Override
    public String getNameById(Integer enterpriseId) {
        LambdaQueryWrapper<Supplier> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Supplier::getName);
        queryWrapper.eq(Supplier::getId, enterpriseId);
        Supplier one = this.getOne(queryWrapper);
        if (ObjectUtil.isNull(one)) {
            return null;
        }
        return one.getName();
    }

}
