package com.arrebol.kanxue.auth.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.arrebol.framework.common.exception.BizException;
import com.arrebol.framework.common.response.Response;
import com.arrebol.framework.common.util.JsonUtil;
import com.arrebol.kanxue.auth.constant.RedisKeyConstants;
import com.arrebol.kanxue.auth.domain.dataobject.UserDO;
import com.arrebol.kanxue.auth.domain.mapper.UserDOMapper;
import com.arrebol.kanxue.auth.enums.LoginTypeEnum;
import com.arrebol.kanxue.auth.enums.ResponseCodeEnum;
import com.arrebol.kanxue.auth.model.vo.user.UserLoginReqVO;
import com.arrebol.kanxue.auth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Description
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
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        // 登录类型
        Integer type = userLoginReqVO.getType();
        // 手机号
        String phone = userLoginReqVO.getPhone();

        Long userId = null;

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);
        switch (loginTypeEnum) {
            // 验证码登录
            case VERIFICATION_CODE:
                // 验证码
                String code = userLoginReqVO.getCode();

                // 校验验证码是否为空
                if (StrUtil.isBlank(code)) {
                    return Response.fail(ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode());
                }

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
                    // todo

                } else {
                    // 已注册，则获取其用户 ID
                    userId = userDO.getId();
                }
                break;

            case PASSWORD: // 密码登录
                // todo

                break;
            default:
                break;
        }
        // SaToken 登录用户，并返回 token 令牌
        // todo

        return Response.success();
    }
}
