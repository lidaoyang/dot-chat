package com.dot.sys.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统角色菜单前端页面使用对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(name = "SysRoleMenuResponse", description = "系统角色菜单前端页面使用对象")
public class SysRoleMenuResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 2670932611965969700L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Integer id;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String text;

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
     * 链接地址(页面菜单和API菜单的地址,文件夹菜单为空)
     */
    @Schema(description = "链接地址(页面菜单和API菜单的地址,文件夹菜单为空)")
    private String url;

    /**
     * 排序编号
     */
    @Schema(description = "排序编号")
    private Integer sort;

    /**
     * 子级菜单列表
     */
    @Schema(description = "子级菜单列表")
    private List<SysRoleMenuResponse> children = new ArrayList<>();

}
