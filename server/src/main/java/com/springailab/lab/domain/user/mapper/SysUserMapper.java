package com.springailab.lab.domain.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springailab.lab.domain.user.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper。
 *
 * @author jiaolin
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {
}
