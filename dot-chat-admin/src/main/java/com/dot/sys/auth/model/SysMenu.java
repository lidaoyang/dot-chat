package com.dot.sys.auth.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统菜单实体
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@TableName("sys_menu")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysMenu", description = "系统菜单")
public class SysMenu implements Serializable {

    @Serial
    private static final long serialVersionUID = 2670932611965969700L;

    /**
     * ID
     */
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
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
    @Schema(description = "菜单类型(0:目录菜单,1:API菜单,2:页面菜单)")
    private Integer type;

    /**
     * 链接地址(页面菜单和API菜单的地址,文件夹菜单为空)
     */
    @Schema(description = "链接地址(页面菜单和API菜单的地址,文件夹菜单为空)")
    private String linkUrl;

    /**
     * 菜单树形层级
     */
    @Schema(description = "菜单树形层级")
    private Integer level;

    /**
     * 树节点的路径,从根节点到当前节点的父级,用逗号分割
     */
    @Schema(description = "树节点的路径,从根节点到当前节点的父级,用逗号分割")
    private String path;

    /**
     * 状态(0:隐藏;1:显示),默认1
     */
    @Schema(description = "状态(0:隐藏;1:显示),默认1")
    private Integer status;

    /**
     * 排序编号
     */
    @Schema(description = "排序编号")
    private Integer sort;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private String updateTime;

}
