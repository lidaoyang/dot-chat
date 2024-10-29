package com.dot.sys.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.comm.em.UserTypeEm;
import com.dot.sys.user.dao.EnterpriseDao;
import com.dot.sys.user.model.Enterprise;
import com.dot.sys.user.response.EnterpriseSimResponse;
import com.dot.sys.user.service.EnterpriseService;
import com.dot.sys.user.service.SystemAdminService;
import com.dot.sys.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 企业信息表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
@Slf4j
@Service
public class EnterpriseServiceImpl extends ServiceImpl<EnterpriseDao, Enterprise> implements EnterpriseService {

    @Resource
    private SystemAdminService systemAdminService;

    @Resource
    private UserService userService;

    @Override
    public List<EnterpriseSimResponse> getSimList(String name, Integer industryType) {
        LambdaQueryWrapper<Enterprise> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(Enterprise::getEnterpriseId, Enterprise::getIndustryType, Enterprise::getName);
        queryWrapper.eq(Enterprise::getStatus, 1);
        queryWrapper.eq(ObjectUtil.isNotNull(industryType), Enterprise::getIndustryType, industryType);
        queryWrapper.like(StringUtils.isNotBlank(name), Enterprise::getName, name);
        queryWrapper.orderByDesc(Enterprise::getEnterpriseId);
        List<Enterprise> enterpriseList = this.list(queryWrapper);
        List<EnterpriseSimResponse> responseList = new ArrayList<>();
        if (CollUtil.isEmpty(enterpriseList)) {
            return responseList;
        }
        return BeanUtil.copyToList(enterpriseList, EnterpriseSimResponse.class);
    }

    @Override
    public List<EnterpriseSimResponse> getSimList(String userType, String phone) {
        List<Integer> enterpriseIdList;
        if (UserTypeEm.ENT_USER.getCode().equals(userType)) {
            enterpriseIdList = userService.getEnterpriseIdList(phone);
        } else {
            enterpriseIdList = systemAdminService.getEnterpriseIdList(UserTypeEm.ENTERPRISE.getCode(), phone);
        }
        if (CollUtil.isNotEmpty(enterpriseIdList)) {
            LambdaQueryWrapper<Enterprise> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.select(Enterprise::getEnterpriseId, Enterprise::getName);
            queryWrapper.eq(Enterprise::getStatus, 1);
            queryWrapper.in(Enterprise::getEnterpriseId, enterpriseIdList);
            queryWrapper.orderByAsc(Enterprise::getEnterpriseId);
            List<Enterprise> enterpriseList = this.list(queryWrapper);
            List<EnterpriseSimResponse> responseList = new ArrayList<>();
            enterpriseList.forEach(enterprise -> {
                EnterpriseSimResponse response = new EnterpriseSimResponse();
                response.setEnterpriseId(enterprise.getEnterpriseId());
                response.setName(enterprise.getName());
                responseList.add(response);
            });
            return responseList;
        }
        return CollUtil.newArrayList();
    }

    @Override
    public String getNameById(Integer enterpriseId) {
        LambdaQueryWrapper<Enterprise> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(Enterprise::getName).eq(Enterprise::getEnterpriseId, enterpriseId);
        Enterprise one = this.getOne(queryWrapper);
        if (ObjectUtil.isNull(one)) {
            return null;
        }
        return one.getName();
    }
}
