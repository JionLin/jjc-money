package com.springailab.lab.domain.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 会话记忆配置。
 *
 * @author jiaolin
 */
@Component
@ConfigurationProperties(prefix = "lab.chat.session")
public class ChatSessionProperties {

    private String keyPrefix = "lab:chat:session:";

    private int maxMessages = 20;

    private long ttlSeconds = 3600;

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
