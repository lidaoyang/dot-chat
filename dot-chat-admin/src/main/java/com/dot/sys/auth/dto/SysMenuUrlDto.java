package com.dot.sys.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统菜单URL对象
 * 
 * @author Dao-yang
 * @date: 2023-07-14 19:07:20
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SysMenuUrlDto", description = "系统菜单URL对象")
public class SysMenuUrlDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 7499059458201415701L;

    @Schema(name = "id")
    private Integer id;

    /**
     * 链接地址(页面菜单和API菜单的地址,文件夹菜单为空)
     */
    @Schema(name = "链接地址(页面菜单和API菜单的地址,文件夹菜单为空)")
    private String linkUrl;

}
