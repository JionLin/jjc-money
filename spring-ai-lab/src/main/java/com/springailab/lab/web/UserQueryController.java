package com.springailab.lab.web;

import com.springailab.lab.domain.user.service.UserQueryService;
import com.springailab.lab.web.dto.UserNameQueryRequest;
import com.springailab.lab.web.dto.UserNameResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 用户查询 API（项目内）。
 *
 * @author jiaolin
 */
@RestController
@RequestMapping("/api/users")
public class UserQueryController {

    private static final Logger log = LoggerFactory.getLogger(UserQueryController.class);

    private final UserQueryService userQueryService;

    /**
     * @param userQueryService 用户查询服务
     */
    public UserQueryController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    /**
     * 按 userId 查询用户名。
     *
     * @param userId 用户ID
     * @return 用户名响应
     */
    @GetMapping("/{userId}/username")
    @ResponseStatus(HttpStatus.OK)
    public UserNameResponse getUsernameByUserId(@PathVariable("userId") Long userId) {
        log.info("Query username by userId={}", userId);
        return this.userQueryService.findUsernameByUserId(userId)
                .map(username -> new UserNameResponse(userId, username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    /**
     * 按 userId 查询用户名（供 Tool 走 POST 调用）。
     *
     * @param request 请求体
     * @return 用户名响应
     */
    @PostMapping("/username-query")
    @ResponseStatus(HttpStatus.OK)
    public UserNameResponse queryUsername(@RequestBody UserNameQueryRequest request) {
        Long userId = request.getUserId();
        log.info("Query username by POST, userId={}", userId);
        return this.userQueryService.findUsernameByUserId(userId)
                .map(username -> new UserNameResponse(userId, username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    }
}
