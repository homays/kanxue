package com.arrebol.kanxue.user.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.arrebol.framework.biz.context.holder.LoginUserContextHolder;
import com.arrebol.framework.common.response.Response;
import com.arrebol.framework.common.util.ParamUtils;
import com.arrebol.kanxue.user.biz.domain.dataobject.UserDO;
import com.arrebol.kanxue.user.biz.domain.mapper.UserDOMapper;
import com.arrebol.kanxue.user.biz.enums.ResponseCodeEnum;
import com.arrebol.kanxue.user.biz.enums.SexEnum;
import com.arrebol.kanxue.user.biz.model.vo.UpdateUserInfoReqVO;
import com.arrebol.kanxue.user.biz.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserDOMapper userDOMapper;

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

        if (Objects.nonNull(avatarFile)) {
            // todo: 调用对象存储服务上传文件
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
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL);
            userDO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
        if (Objects.nonNull(birthday)) {
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
        if (Objects.nonNull(backgroundImgFile)) {
            // todo: 调用对象存储服务上传文件
        }

        if (needUpdate) {
            // 更新用户信息
            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);
        }
        return Response.success();
    }
}