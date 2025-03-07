package com.dot.sys.auth.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dot.sys.auth.model.SysRoleDetail;
import com.dot.sys.auth.vo.SysPermissionVo;
import com.dot.sys.auth.vo.SysRoleMenuVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色明细数据Mapper
 * 
 * @author Dao-yang
 * @date: 2025-03-04 11:34:34
 */
public interface SysRoleDetailDao extends BaseMapper<SysRoleDetail> {

    @Select("select me.id,me.name,me.link_url as url,rd.role_id "
            + "from sys_role_detail rd "
            + "inner join sys_menu me on me.id=rd.menu_id "
            + "${ew.customSqlSegment} ")
    List<SysPermissionVo> selectSysPermissionList(@Param(Constants.WRAPPER) QueryWrapper<SysPermissionVo> queryWrapper);

    @Select("select me.id,me.name,me.link_url,me.icon_cls,me.type,me.pid,me.sort,rd.role_id "
            + "from sys_role_detail rd "
            + "inner join sys_menu me on me.id=rd.menu_id "
            + "${ew.customSqlSegment} ")
    List<SysRoleMenuVo> selectSysRoleMenuList(@Param(Constants.WRAPPER) QueryWrapper<SysRoleMenuVo> queryWrapper);

}
