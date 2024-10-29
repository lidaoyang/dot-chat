package com.dot.sys.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.sys.user.model.SystemAdmin;

import java.util.List;

/**
 * 后台管理员表服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
public interface SystemAdminService extends IService<SystemAdmin> {

    SystemAdmin getAdmin(String type, Integer enterpriseId, String account);

    List<Integer> getEnterpriseIdList(String type, String account);
}
