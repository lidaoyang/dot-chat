package com.dot.sys.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.sys.user.model.Enterprise;
import com.dot.sys.user.response.EnterpriseSimResponse;

import java.util.List;

/**
 * 企业信息表服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
public interface EnterpriseService extends IService<Enterprise> {

    List<EnterpriseSimResponse> getSimList(String name, Integer industryType);

    List<EnterpriseSimResponse> getSimList(String userType, String phone);

    String getNameById(Integer enterpriseId);
}
