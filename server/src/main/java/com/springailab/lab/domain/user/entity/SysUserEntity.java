package com.springailab.lab.domain.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 系统用户实体（映射 sys_user）。
 *
 * @author jiaolin
 */
@TableName("sys_user")
public class SysUserEntity {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @TableField("username")
    private String username;

    /**
     * @return 用户ID
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return 用户名
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
