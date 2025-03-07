package com.dot.comm.entity;

import com.dot.comm.em.ExceptionCodeEm;
import com.dot.comm.exception.ApiException;
import com.dot.comm.utils.CommUtil;
import com.dot.sys.auth.em.RoleTypeEm;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

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
     * 账号
     */
    private String account;

    /**
     * 用户ID
     */
    private Integer uid;

    /**
     * 角色ids
     */
    private String roles;

    /**
     * 角色类型(0:超级管理员,1:普通管理员,2:演示管理员)
     */
    private Integer roleType;


    private static final String SPLIT_CHAR = ":";

    public LoginUsername(Integer uid, String account, String roles, Integer roleType) {
        this.uid = uid;
        this.account = account;
        this.roles = roles;
        this.roleType = roleType;
    }

    /**
     * 合并用户名(uid,account,sex,nickname)
     *
     * @return 合并用户名
     */
    public String mergeUsername() {
        return uid + SPLIT_CHAR + account + SPLIT_CHAR + roles + SPLIT_CHAR + roleType;
    }

    public static LoginUsername splitUsername(String username) {
        String[] us = username.split(SPLIT_CHAR);
        if (us.length < 4) {
            log.error("账号错误,username:{}", username);
            throw new ApiException(ExceptionCodeEm.NOT_FOUND, "账号不存在");
        }
        return new LoginUsername(Integer.parseInt(us[0]), us[1], us[2], Integer.parseInt(us[3]));
    }

    /**
     * 是否是超级管理员(行业为0的管理员)
     *
     * @return true/false
     */
    public boolean isSuperAdmin() {
        return RoleTypeEm.SUPER_ADMIN.getCode() == roleType;
    }

    /**
     * 是否是演示管理员
     *
     * @return true/false
     */
    public boolean isDemoAdmin() {
        return RoleTypeEm.DEMO_ADMIN.getCode() == roleType;
    }

    public List<Integer> getRoleList() {
        return CommUtil.stringToArrayInt(roles);
    }


}
