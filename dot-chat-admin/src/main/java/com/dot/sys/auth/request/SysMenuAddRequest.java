package com.dot.sys.auth.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统菜单添加请求对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysMenuAddRequest", description = "系统菜单添加请求对象")
public class SysMenuAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 2670932611965969700L;


    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID")
    private Integer pid;

    /**
     * 菜单图标CSS类
     */
    @Schema(description = "菜单图标CSS类")
    private String iconCls;

    /**
     * 菜单类型(0:目录菜单,1:API菜单,2:页面菜单)
     */
    @Schema(description = "菜单类型(0:目录菜单,1:API菜单,2:页面菜单)", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"0", "1", "2"})
    @NotNull(message = "菜单类型不能为空")
    private Integer type;

    /**
     * 链接地址(页面菜单和API菜单的地址,文件夹菜单为空)
     */
    @Schema(description = "链接地址(页面菜单和API菜单的地址,文件夹菜单为空)")
    private String linkUrl;

    /**
     * 状态(0:隐藏;1:显示),默认1
     */
    @Schema(description = "状态(0:隐藏;1:显示),默认1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Boolean status;

    /**
     * 排序编号
     */
    @Schema(description = "排序编号")
    private Integer sort;


}
