package com.dot.sys.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: Dao-yang.
 * @date: Created in 2023/3/26 19:13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysRoleMenuDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 4887024225864745865L;

    /**
     * 菜单ID
     */
    private Integer id;

    /**
     * 角色ID
     */
    private Integer roleId;

    /**
     * 权限名称
     */
    private String text;

    /**
     * 权限链接
     */
    private String url;

    /**
     * 父级ID
     */
    private Integer pid;

    /**
     * 菜单图标CSS类
     */
    private String iconCls;

    /**
     * 菜单类型(0:目录菜单,1:API菜单,2:页面菜单)
     */
    private Integer type;

    /**
     * 排序编号
     */
    private Integer sort;
}
