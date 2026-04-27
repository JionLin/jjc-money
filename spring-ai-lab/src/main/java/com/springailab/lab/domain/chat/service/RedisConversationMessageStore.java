package com.springailab.lab.domain.chat.service;

import com.springailab.lab.domain.chat.config.ChatSessionProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 会话消息存储实现。
 *
 * @author jiaolin
 */
@Service
public class RedisConversationMessageStore implements ConversationMessageStore {

    private final StringRedisTemplate stringRedisTemplate;

    private final ChatSessionProperties properties;

    public RedisConversationMessageStore(StringRedisTemplate stringRedisTemplate, ChatSessionProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.properties = properties;
    }

    @Override
    public List<String> loadMessages(String conversationId) {
        String key = buildKey(conversationId);
        Long size = this.stringRedisTemplate.opsForList().size(key);
        if (size == null || size <= 0) {
            return Collections.emptyList();
        }
        List<String> values = this.stringRedisTemplate.opsForList().range(key, 0, -1);
        if (values == null) {
            return Collections.emptyList();
        }
        return values;
    }

    @Override
    public void appendMessage(String conversationId, String message) {
        if (!StringUtils.hasText(message)) {
            return;
        }
        String key = buildKey(conversationId);
        this.stringRedisTemplate.opsForList().rightPush(key, message);
        this.stringRedisTemplate.opsForList().trim(key, -this.properties.getMaxMessages(), -1);
        this.stringRedisTemplate.expire(key, this.properties.getTtlSeconds(), TimeUnit.SECONDS);
    }

    private String buildKey(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return this.properties.getKeyPrefix() + "anonymous";
        }
        return this.properties.getKeyPrefix() + conversationId.trim();
    }
}
