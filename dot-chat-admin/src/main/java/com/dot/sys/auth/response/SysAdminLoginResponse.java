package com.dot.sys.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员列表返回对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysAdminLoginResponse", description = "管理员登录返回对象")
public class SysAdminLoginResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 9090686573093972664L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Integer id;

    /**
     * 账号
     */
    @Schema(description = "账号")
    private String account;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;

    /**
     * 角色类型(0:超级管理员,1:普通管理员,2:演示管理员)
     */
    @Schema(description = "角色类型(0:超级管理员,1:普通管理员,2:演示管理员)")
    private Integer roleType;

}
