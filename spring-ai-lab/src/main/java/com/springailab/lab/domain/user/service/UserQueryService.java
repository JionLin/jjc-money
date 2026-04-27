package com.springailab.lab.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.springailab.lab.domain.user.entity.SysUserEntity;
import com.springailab.lab.domain.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户查询服务。
 *
 * @author jiaolin
 */
@Service
public class UserQueryService {

    private final SysUserMapper sysUserMapper;

    /**
     * @param sysUserMapper 用户 Mapper
     */
    public UserQueryService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 按 userId 查询用户名。
     *
     * @param userId 用户ID
     * @return 用户名（不存在时为空）
     */
    public Optional<String> findUsernameByUserId(Long userId) {
        if (userId == null || userId <= 0L) {
            return Optional.empty();
        }
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getUserId, userId)
                .select(SysUserEntity::getUserId, SysUserEntity::getUsername)
                .last("LIMIT 1");
        SysUserEntity user = this.sysUserMapper.selectOne(wrapper);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(user.getUsername());
    }
}
