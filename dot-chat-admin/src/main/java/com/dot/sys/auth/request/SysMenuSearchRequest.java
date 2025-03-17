package com.dot.sys.auth.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 系统菜单搜索请求对象
 *
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysMenuSearchRequest", description = "系统菜单搜索请求对象")
public class SysMenuSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 2670932611965969700L;

    /**
     * 关键词搜索(id,name,url)
     */
    @Schema(description = "关键词搜索(id,name,url)")
    private String keywords;

    /**
     * 状态(false:隐藏;true:显示),默认true
     */
    @Schema(description = "状态(false:隐藏;true:显示),默认true")
    private Boolean status;

    /**
     * 菜单树形层级
     */
    @Schema(description = "菜单树形层级")
    private Integer level;

    /**
     * 菜单类型面(0:目录菜单,1:API菜单,2:页面菜单)
     */
    @Schema(description = "菜单类型(0:目录菜单,1:API菜单,2:页面菜单)", allowableValues = {"0", "1", "2"})
    private Integer type;

    /**
     * 菜单类型面(0:目录菜单,1:API菜单,2:页面菜单)
     */
    @JsonIgnore
    @Schema(description = "菜单类型(0:目录菜单,1:API菜单,2:页面菜单)", allowableValues = {"0", "1", "2"}, hidden = true)
    private List<Integer> types;

}
