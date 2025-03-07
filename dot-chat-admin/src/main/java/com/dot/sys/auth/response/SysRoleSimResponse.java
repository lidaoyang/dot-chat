package com.dot.sys.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统角色返回对象(精简)
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysRoleSimResponse", description = "系统角色返回对象(精简)")
public class SysRoleSimResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 7103375797647840549L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Integer id;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String name;

}
