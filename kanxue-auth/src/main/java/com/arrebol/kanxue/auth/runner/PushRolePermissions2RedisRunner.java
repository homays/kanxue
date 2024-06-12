package com.arrebol.kanxue.auth.runner;

import cn.hutool.core.collection.CollUtil;
import com.arrebol.framework.common.util.JsonUtil;
import com.arrebol.kanxue.auth.constant.RedisKeyConstants;
import com.arrebol.kanxue.auth.domain.dataobject.PermissionDO;
import com.arrebol.kanxue.auth.domain.dataobject.RoleDO;
import com.arrebol.kanxue.auth.domain.dataobject.RolePermissionDO;
import com.arrebol.kanxue.auth.domain.mapper.PermissionDOMapper;
import com.arrebol.kanxue.auth.domain.mapper.RoleDOMapper;
import com.arrebol.kanxue.auth.domain.mapper.RolePermissionDOMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推送角色权限数据到 Redis 中
 */
@Component
@Slf4j
public class PushRolePermissions2RedisRunner implements ApplicationRunner {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private PermissionDOMapper permissionDOMapper;
    @Resource
    private RolePermissionDOMapper rolePermissionDOMapper;

    // 权限同步标记 Key （防止集群部署，多次加载）
    private static final String PUSH_PERMISSION_FLAG = "push.permission.flag";

    @Override
    public void run(ApplicationArguments args) {
        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");

        try {
            // 是否能够同步数据: 原子操作，只有在键 PUSH_PERMISSION_FLAG 不存在时，才会设置该键的值为 "1"，并设置过期时间为 1 天
            boolean canPushed = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(PUSH_PERMISSION_FLAG, "1", 1, TimeUnit.DAYS));

            // 如果无法同步权限数据
            if (!canPushed) {
                log.warn("==> 角色权限数据已经同步至 Redis 中，不再同步...");
                return;
            }

            // 获取所有角色
            List<RoleDO> roleList = roleDOMapper.selectEnabledList();

            // 获取所有角色 ID
            List<Long> roleIds = roleList.stream().map(RoleDO::getId).toList();

            if (CollUtil.isNotEmpty(roleIds)) {
                // 通过角色ID 获取所有 角色-权限关联关系
                List<RolePermissionDO> rolePermissionList = rolePermissionDOMapper.selectByRoleIds(roleIds);

                // 按角色 ID 分组, 每个角色 ID 对应多个权限 ID
                Map<Long, List<Long>> roleIdPermissionIdsMap = rolePermissionList.stream().collect(
                        Collectors.groupingBy(RolePermissionDO::getRoleId,
                                Collectors.mapping(RolePermissionDO::getPermissionId, Collectors.toList()))
                );

                // 获取所有被启用权限
                List<PermissionDO> permissionList = permissionDOMapper.selectAppEnabledList();

                // 权限 ID - 权限 DO 映射
                Map<Long, PermissionDO> permissionIdDOMap = permissionList.stream().collect(
                        Collectors.toMap(PermissionDO::getId, permissionDO -> permissionDO)
                );

                // 组织 角色ID-权限 关系
                Map<Long, List<PermissionDO>> roleIdPermissionMap = Maps.newHashMap();

                roleList.forEach(roleDO -> {
                    // 当前角色 ID
                    Long roleId = roleDO.getId();
                    // 当前角色 ID 对应的权限 ID 集合
                    List<Long> permissionIdList = roleIdPermissionIdsMap.get(roleId);
                    if (CollUtil.isNotEmpty(permissionIdList)) {
                        List<PermissionDO> permissionDOList = Lists.newArrayList();
                        permissionIdList.forEach(permissionId -> {
                            PermissionDO permissionDO = permissionIdDOMap.get(permissionId);
                            permissionDOList.add(permissionDO);
                        });
                        roleIdPermissionMap.put(roleId, permissionDOList);
                    }
                });

                // 当前角色 ID 对应的权限 ID 集合
                roleIdPermissionMap.forEach((roleId, permissionDO) -> {
                    String key = RedisKeyConstants.buildRolePermissionsKey(roleId);
                    redisTemplate.opsForValue().set(key, JsonUtil.toJsonString(permissionDO));
                });
            }
        } catch (Exception e) {
            log.error("==> 同步角色权限数据到 Redis 中失败: ", e);
        }

        log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
    }
}