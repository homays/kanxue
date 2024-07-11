package com.arrebol.kanxue.user.biz.domain.mapper;

import com.arrebol.kanxue.user.biz.domain.dataobject.RolePermissionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolePermissionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RolePermissionDO record);

    int insertSelective(RolePermissionDO record);

    RolePermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermissionDO record);

    int updateByPrimaryKey(RolePermissionDO record);

    /**
     * 根据角色id 查询所有权限
     */
    List<RolePermissionDO> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
}