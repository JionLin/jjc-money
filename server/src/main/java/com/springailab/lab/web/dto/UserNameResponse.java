package com.springailab.lab.web.dto;

/**
 * 用户名查询响应。
 *
 * @author jiaolin
 */
public class UserNameResponse {

    private Long userId;

    private String username;

    /**
     * @param userId 用户ID
     * @param username 用户名
     */
    public UserNameResponse(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

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
