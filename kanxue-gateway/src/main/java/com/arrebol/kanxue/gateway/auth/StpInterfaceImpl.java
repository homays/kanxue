package com.arrebol.kanxue.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.arrebol.kanxue.gateway.constant.RedisKeyConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public List<String> getPermissionList(Object loginId, String loginType) {
        log.info("## 获取用户权限列表, loginId: {}", loginId);

        // 构建 用户-角色 Redis Key
        String userRolesKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));

        // 根据用户 ID ，从 Redis 中获取该用户的角色集合
        String useRolesValue = redisTemplate.opsForValue().get(userRolesKey);

        if (StringUtils.isBlank(useRolesValue)) {
            return null;
        }

        // 将 JSON 字符串转换为 List<String> 角色集合
        List<String> userRoleKeys = objectMapper.readValue(useRolesValue, new TypeReference<>() {});

        if (!CollectionUtils.isEmpty(userRoleKeys)) {
            // 查询这些角色对应的权限
            // 构建 角色-权限 Redis Key 集合
            List<String> rolePermissionKeys = userRoleKeys.stream()
                    .map(RedisKeyConstants::buildRolePermissionsKey).toList();

            // 通过 key 集合批量查询权限，提升查询性能。
            List<String> rolePermissionsValues = redisTemplate.opsForValue().multiGet(rolePermissionKeys);

            if (!CollectionUtils.isEmpty(rolePermissionsValues)) {
                List<String> permissions = Lists.newArrayList();

                // 遍历所有角色的权限集合，统一添加到 permissions 集合中
                rolePermissionsValues.forEach(jsonValue -> {
                    try {
                        List<String> rolePermissions = objectMapper.readValue(jsonValue, new TypeReference<>() {});
                        permissions.addAll(rolePermissions);
                    } catch (JsonProcessingException e) {
                        log.error("==> JSON 解析错误: ", e);
                    }
                });

                // 返回此用户所拥有的权限
                return permissions;
            }
        }
        return null;
    }

    @Override
    @SneakyThrows
    public List<String> getRoleList(Object loginId, String loginType) {
        log.info("## 获取用户角色列表，loginId：{}", loginId);

        // 构建 用户-角色 Redis Key
        String userRoleKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));

        // 根据用户 ID，从 Redis 中获取该用户的角色集合
        String userRolesValue = redisTemplate.opsForValue().get(userRoleKey);

        if (StringUtils.isBlank(userRolesValue)) {
            return null;
        }

        // 将 JSON 字符串转换为 List<String> 集合
        return objectMapper.readValue(userRolesValue, new TypeReference<>() {});
    }

}