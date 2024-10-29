package com.dot.sys.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.sys.user.model.Supplier;
import com.dot.sys.user.response.SupplierSimResponse;

import java.util.List;

/**
 * 供应商表服务接口
 * 
 * @author Dao-yang
 * @date: 2024-01-10 10:29:35
 */
public interface SupplierService extends IService<Supplier> {

    List<SupplierSimResponse> getSimList(String name, Integer industryType);

    List<SupplierSimResponse> getSimList(String phone);

    String getNameById(Integer enterpriseId);

}
