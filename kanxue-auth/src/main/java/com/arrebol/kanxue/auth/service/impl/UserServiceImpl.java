package com.arrebol.kanxue.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.arrebol.framework.biz.context.holder.LoginUserContextHolder;
import com.arrebol.framework.common.constant.GlobalConstants;
import com.arrebol.framework.common.enums.DeletedEnum;
import com.arrebol.framework.common.enums.StatusEnum;
import com.arrebol.framework.common.exception.BizException;
import com.arrebol.framework.common.response.Response;
import com.arrebol.framework.common.util.JsonUtil;
import com.arrebol.kanxue.auth.constant.RedisKeyConstants;
import com.arrebol.kanxue.auth.constant.RoleConstants;
import com.arrebol.kanxue.auth.domain.dataobject.RoleDO;
import com.arrebol.kanxue.auth.domain.dataobject.UserDO;
import com.arrebol.kanxue.auth.domain.dataobject.UserRoleDO;
import com.arrebol.kanxue.auth.domain.mapper.RoleDOMapper;
import com.arrebol.kanxue.auth.domain.mapper.UserDOMapper;
import com.arrebol.kanxue.auth.domain.mapper.UserRoleDOMapper;
import com.arrebol.kanxue.auth.enums.LoginTypeEnum;
import com.arrebol.kanxue.auth.enums.ResponseCodeEnum;
import com.arrebol.kanxue.auth.model.vo.user.UpdatePasswordReqVO;
import com.arrebol.kanxue.auth.model.vo.user.UserLoginReqVO;
import com.arrebol.kanxue.auth.service.UserService;
import com.google.common.base.Preconditions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现类
 *
 * @author Arrebol
 * @date 2024/6/10
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserDOMapper userDOMapper;
    @Resource
    private UserRoleDOMapper userRoleDOMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        // 登录类型
        Integer type = userLoginReqVO.getType();
        // 手机号
        String phone = userLoginReqVO.getPhone();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);

        if (ObjectUtil.isEmpty(loginTypeEnum)) {
            throw new BizException(ResponseCodeEnum.LOGIN_TYPE_ERROR);
        }

        Long userId = null;

        switch (loginTypeEnum) {
            // 验证码登录
            case VERIFICATION_CODE:
                // 验证码
                String code = userLoginReqVO.getCode();

                // 校验验证码是否为空
                Preconditions.checkArgument(StrUtil.isNotBlank(code), "验证码不能为空");

                // 构建验证码 redis key
                String key = RedisKeyConstants.buildVerificationCodeKey(phone);

                // 从redis 中取出验证码
                String sentCode = (String) redisTemplate.opsForValue().get(key);

                // 判断用户提交的验证码，与 Redis 中的验证码是否一致
                if (!StrUtil.equals(sentCode, code)) {
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }

                UserDO userDO = userDOMapper.selectByPhone(phone);

                log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtil.toJsonString(userDO));

                // 判断是否注册
                if (ObjUtil.isNull(userDO)) {
                    // 若此用户还没有注册，系统自动注册该用户
                    userId = registerUser(phone);
                } else {
                    // 已注册，则获取其用户 ID
                    userId = userDO.getId();
                }
                // 删除验证码
                redisTemplate.delete(key);
                break;

            case PASSWORD: // 密码登录
                String password = userLoginReqVO.getPassword();
                // 根据手机号查询用户
                UserDO dbUser = userDOMapper.selectByPhone(phone);
                // 判断用户是否存在
                if (ObjectUtil.isEmpty(dbUser)) {
                    throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
                }

                // 用户数据库中的密码
                String dbPwd = dbUser.getPassword();

                // 匹配密码是否一致
                boolean isPwdCorrect = passwordEncoder.matches(password, dbPwd);

                if (!isPwdCorrect) {
                    throw new BizException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
                }

                userId = dbUser.getId();
                break;
            default:
                break;
        }
        // SaToken 登录用户, 入参为用户 ID
        StpUtil.login(userId);

        // 获取 Token 令牌
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 返回 Token 令牌
        return Response.success(tokenInfo.tokenValue);
    }

    @Override
    public Response<?> logout() {
        StpUtil.logout(LoginUserContextHolder.getUserId());
        return Response.success();
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO) {
        // 新密码
        String newPassword = updatePasswordReqVO.getNewPassword();
        // 密码加密
        String encodePwd = passwordEncoder.encode(newPassword);

        // 获取当前用户 ID
        Long userId = LoginUserContextHolder.getUserId();

        UserDO userDO = UserDO.builder()
                .id(userId)
                .password(encodePwd)
                .updateTime(LocalDateTime.now())
                .build();

        // 更新密码
        userDOMapper.updateByPrimaryKeySelective(userDO);

        return Response.success();
    }

    public Long registerUser(String phone) {
        return transactionTemplate.execute(status -> {
            try {
                // 获取全局自增的看雪 ID
                Long kanxueId = redisTemplate.opsForValue().increment(RedisKeyConstants.KANXUE_ID_GENERATOR_KEY);

                UserDO userDO = UserDO.builder()
                        .phone(phone)
                        .kanxueId(String.valueOf(kanxueId)) // 自动生成看雪号 ID
                        .nickname(GlobalConstants.NICKNAME_PREFIX + kanxueId) // 自动生成昵称, 如：看雪_10000
                        .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                        .build();

                // 保存用户信息
                userDOMapper.insert(userDO);

                // 获取刚刚添加入库的用户 ID
                Long userId = userDO.getId();

                // 给该用户分配一个默认角色
                UserRoleDO userRoleDO = UserRoleDO.builder()
                        .userId(userId)
                        .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue())
                        .build();
                userRoleDOMapper.insert(userRoleDO);

                RoleDO roleDO = roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);

                // 将该用户的角色 ID 存入 Redis 中
                List<String> roles = new ArrayList<>(1);
                roles.add(roleDO.getRoleKey());
                String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
                redisTemplate.opsForValue().set(userRolesKey, JsonUtil.toJsonString(roles));

                return userId;
            } catch (Exception e) {
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("==> 系统注册用户异常: ", e);
                return null;
            }
        });
    }
}
