package com.arrebol.kanxue.auth.domain.mapper;

import com.arrebol.kanxue.auth.domain.dataobject.UserDO;

public interface UserDOMapper {

    /**
     * 根据主键 ID 查询
     */
    UserDO selectByPrimaryKey(Long id);

    /**
     * 根据主键 ID 删除
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 插入记录
     */
    int insert(UserDO record);

    /**
     * 更新记录
     */
    int updateByPrimaryKey(UserDO record);
}