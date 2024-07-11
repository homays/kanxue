package com.arrebol.kanxue.user.biz.domain.mapper;

import com.arrebol.kanxue.user.biz.domain.dataobject.PermissionDO;

import java.util.List;

public interface PermissionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PermissionDO record);

    int insertSelective(PermissionDO record);

    PermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PermissionDO record);

    int updateByPrimaryKey(PermissionDO record);

    /**
     * 查询所有被启用的按钮(type = 3)权限
     */
    List<PermissionDO> selectAppEnabledList();
}