package com.springailab.lab.web.dto;

/**
 * 用户名查询请求（供 Tool 走 POST 调用）。
 *
 * @author jiaolin
 */
public class UserNameQueryRequest {

    private Long userId;

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
