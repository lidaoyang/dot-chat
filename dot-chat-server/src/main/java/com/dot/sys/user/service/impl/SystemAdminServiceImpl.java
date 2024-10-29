package com.dot.sys.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dot.sys.user.dao.SystemAdminDao;
import com.dot.sys.user.model.SystemAdmin;
import com.dot.sys.user.service.SystemAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台管理员表服务接口实现
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
@Slf4j
@Service
public class SystemAdminServiceImpl extends ServiceImpl<SystemAdminDao, SystemAdmin> implements SystemAdminService {


    @Override
    public SystemAdmin getAdmin(String type, Integer enterpriseId, String account) {
        LambdaQueryWrapper<SystemAdmin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SystemAdmin::getType, type);
        wrapper.eq(SystemAdmin::getEnterpriseId, enterpriseId);
        wrapper.eq(SystemAdmin::getAccount, account);
        return this.getOne(wrapper);
    }

    @Override
    public List<Integer> getEnterpriseIdList(String type, String account) {
        LambdaQueryWrapper<SystemAdmin> wrapper = Wrappers.lambdaQuery();
        wrapper.select(SystemAdmin::getEnterpriseId);
        wrapper.eq(SystemAdmin::getType,type);
        wrapper.eq(SystemAdmin::getAccount,account);
        return this.listObjs(wrapper);
    }
}
