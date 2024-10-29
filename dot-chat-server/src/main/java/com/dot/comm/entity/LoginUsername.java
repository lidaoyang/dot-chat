package com.dot.comm.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dot.comm.constants.IndustryTypeConstant;
import com.dot.comm.em.UserTypeEm;
import com.dot.comm.em.UserRoleEm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员用户登录对象
 */
@Slf4j
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginUsername implements Serializable {

    @Serial
    private static final long serialVersionUID = 905784513600880932L;

    /**
     * 后台管理员账号
     */
    private String account;

    /**
     * 管理员类型(ENTERPRISE:服务商企业;SUPPLIER:供应商;PL_ADMIN:平台管理员;ENT_USER:企业用户)
     */
    private String type;
    /**
     * 行业分类(1:礼品行业;2:医疗行业;等等)
     */
    private Integer industryType;

    /**
     * 企业id，总平台管理员的企业id是0
     */
    private Integer enterpriseId;

    /**
     * 管理员ID
     */
    private Integer userId;

    /**
     * 角色ids
     */
    private String roles;

    public LoginUsername(String type, String account, Integer industryType, Integer enterpriseId, Integer userId, String roles) {
        this.type = type;
        this.account = account;
        this.industryType = industryType;
        this.enterpriseId = enterpriseId;
        this.userId = userId;
        this.roles = roles;
    }

    /**
     * 合并用户名(type:account:industryType:enterpriseId:adminId)
     * 
     * @return 合并用户名
     */
    public String mergeUsername() {
        return type + ":" + account + ":" + industryType + ":" + enterpriseId + ":" + userId + ":" + roles;
    }

    public static LoginUsername splitUsername(String username) {
        String[] us = username.split(":");
        if (us.length < 6) {
            log.error("账号错误,username:{}", username);
            throw new RuntimeException("账号不存在");
        }
        return new LoginUsername(us[0], us[1], Integer.parseInt(us[2]), Integer.parseInt(us[3]), Integer.parseInt(us[4]), us[5]);
    }

    public List<Integer> getRoleList() {
        if (StringUtils.isBlank(this.roles)) {
            return new ArrayList<>();
        }
        String[] roleArr = this.roles.split(",");
        return Arrays.stream(roleArr).map(Integer::parseInt).distinct().collect(Collectors.toList());
    }

    /**
     * 是否是管理员
     * 
     * @return true/false
     */
    public boolean isAdmin() {
        return this.type.equals(UserTypeEm.PL_ADMIN.getCode());
    }

    /**
     * 是否是企业
     * 
     * @return true/false
     */
    public boolean isEnterprise() {
        return this.type.equals(UserTypeEm.ENTERPRISE.getCode());
    }

    /**
     * 是否是供应商
     * 
     * @return true/false
     */
    public boolean isSupplier() {
        return this.type.equals(UserTypeEm.SUPPLIER.getCode());
    }

    /**
     * 是否是企业的用户
     * 
     * @return true/false
     */
    public boolean isEntUser() {
        return this.type.equals(UserTypeEm.ENT_USER.getCode());
    }

    /**
     * 是否是超级管理员(行业为0的管理员)
     * 
     * @return true/false
     */
    public boolean isSuperAdmin() {
        if (this.industryType == null) {
            return false;
        }
        return UserTypeEm.PL_ADMIN.getCode().equals(type) && this.industryType == IndustryTypeConstant.ADMIN;
    }

    /**
     * 是否是游客
     *
     * @return true/false
     */
    public boolean isVisitor() {
        return UserTypeEm.ENT_USER.getCode().equals(type) && UserRoleEm.VISITOR.getId().equals(roles);
    }

    /**
     * 是否是会员/员工
     *
     * @return true/false
     */
    public boolean isVipAStaff() {
        return UserTypeEm.ENT_USER.getCode().equals(type) && (UserRoleEm.STAFF.getId().equals(roles) || UserRoleEm.VIP.getId().equals(roles));
    }

    /**
     * 是否是会员/员工
     *
     * @return true/false
     */
    public boolean isVip() {
        return UserTypeEm.ENT_USER.getCode().equals(type) && UserRoleEm.VIP.getId().equals(roles);
    }

    /**
     * 是否是采购员
     *
     * @return true/false
     */
    public boolean isPurchaser() {
        return UserTypeEm.ENT_USER.getCode().equals(type) && UserRoleEm.PURCHASER.getId().equals(roles);
    }
}
