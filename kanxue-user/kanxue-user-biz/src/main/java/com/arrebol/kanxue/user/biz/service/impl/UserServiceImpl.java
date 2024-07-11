package com.arrebol.kanxue.user.biz.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.arrebol.framework.biz.context.holder.LoginUserContextHolder;
import com.arrebol.framework.common.constant.GlobalConstants;
import com.arrebol.framework.common.enums.DeletedEnum;
import com.arrebol.framework.common.enums.StatusEnum;
import com.arrebol.framework.common.exception.BizException;
import com.arrebol.framework.common.response.Response;
import com.arrebol.framework.common.util.JsonUtil;
import com.arrebol.framework.common.util.ParamUtils;
import com.arrebol.kanxue.user.biz.constant.RedisKeyConstants;
import com.arrebol.kanxue.user.biz.constant.RoleConstants;
import com.arrebol.kanxue.user.biz.domain.dataobject.RoleDO;
import com.arrebol.kanxue.user.biz.domain.dataobject.UserDO;
import com.arrebol.kanxue.user.biz.domain.dataobject.UserRoleDO;
import com.arrebol.kanxue.user.biz.domain.mapper.RoleDOMapper;
import com.arrebol.kanxue.user.biz.domain.mapper.UserDOMapper;
import com.arrebol.kanxue.user.biz.domain.mapper.UserRoleDOMapper;
import com.arrebol.kanxue.user.biz.enums.ResponseCodeEnum;
import com.arrebol.kanxue.user.biz.enums.SexEnum;
import com.arrebol.kanxue.user.biz.model.vo.UpdateUserInfoReqVO;
import com.arrebol.kanxue.user.biz.rpc.OssRpcService;
import com.arrebol.kanxue.user.biz.service.UserService;
import com.arrebol.kanxue.user.dto.req.RegisterUserReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserDOMapper userDOMapper;
    @Autowired
    private OssRpcService ossRpcService;
    @Resource
    private UserRoleDOMapper userRoleDOMapper;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 更新用户信息
     */
    @Override
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        UserDO userDO = new UserDO();
        // 设置当前需要更新的用户 ID
        userDO.setId(LoginUserContextHolder.getUserId());
        // 标识位：是否需要更新
        boolean needUpdate = false;
        
        // 头像
        MultipartFile avatarFile = updateUserInfoReqVO.getAvatar();

        if (ObjectUtil.isNotNull(avatarFile)) {
            String avatar = ossRpcService.uploadFile(avatarFile);
            log.info("==> 调用 oss 服务成功，上传头像，url：{}", avatar);

            // 若上传头像失败，则抛出业务异常
            if (StrUtil.isBlank(avatar)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }

            userDO.setAvatar(avatar);
            needUpdate = true;
        }

        // 昵称
        String nickname = updateUserInfoReqVO.getNickname();
        if (StrUtil.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickname(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL);
            userDO.setNickname(nickname);
            needUpdate = true;
        }

        // 小哈书号
        String kanxueId = updateUserInfoReqVO.getKanxueId();
        if (StrUtil.isNotBlank(kanxueId)) {
            Preconditions.checkArgument(ParamUtils.checkKanxueId(kanxueId), ResponseCodeEnum.KANXUE_ID_VALID_FAIL);
            userDO.setKanxueId(kanxueId);
            needUpdate = true;
        }

        // 性别
        Integer sex = updateUserInfoReqVO.getSex();
        if (ObjectUtil.isNotNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL);
            userDO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
        if (ObjectUtil.isNotNull(birthday)) {
            userDO.setBirthday(birthday);
            needUpdate = true;
        }

        // 个人简介
        String introduction = updateUserInfoReqVO.getIntroduction();
        if (StrUtil.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL);
            userDO.setIntroduction(introduction);
            needUpdate = true;
        }

        // 背景图
        MultipartFile backgroundImgFile = updateUserInfoReqVO.getBackgroundImg();
        if (ObjectUtil.isNotNull(backgroundImgFile)) {
            String backgroundImg = ossRpcService.uploadFile(backgroundImgFile);
            log.info("==> 调用 oss 服务成功，上传背景图，url：{}", backgroundImg);

            // 若上传背景图失败，则抛出业务异常
            if (StrUtil.isBlank(backgroundImg)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }

            userDO.setBackgroundImg(backgroundImg);
            needUpdate = true;
        }

        if (needUpdate) {
            // 更新用户信息
            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);
        }
        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> register(RegisterUserReqDTO registerUserReqDTO) {
        String phone = registerUserReqDTO.getPhone();

        // 先判断用户是否注册
        UserDO dbUser = userDOMapper.selectByPhone(phone);

        // 如果用户已经注册，则直接返回用户ID
        if (ObjectUtil.isNotNull(dbUser)) {
            return Response.success(dbUser.getId());
        }

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

        return Response.success(userId);
    }
}