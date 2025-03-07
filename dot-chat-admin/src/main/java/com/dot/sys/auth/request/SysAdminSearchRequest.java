package com.dot.sys.auth.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员搜索请求对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysAdminSearchRequest", description = "管理员搜索请求对象")
public class SysAdminSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 9090686573093972664L;


    /**
     * 搜索关键词(id,name,account)
     */
    @Schema(description = "搜索关键词(id,name,account)")
    private String keywords;


    /**
     * 状态(1:正常,0:禁用)
     */
    @Schema(description = "状态(1:正常,0:禁用)", allowableValues = {"true", "false"})
    private Boolean status;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private Integer roleId;

}
