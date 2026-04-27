package com.springailab.lab.domain.chat.service;

import com.springailab.lab.domain.chat.config.ChatSessionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RedisConversationMessageStore} 测试。
 *
 * @author jiaolin
 */
@ExtendWith(MockitoExtension.class)
class RedisConversationMessageStoreTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    private RedisConversationMessageStore store;

    private ChatSessionProperties properties;

    @BeforeEach
    void setUp() {
        this.properties = new ChatSessionProperties();
        this.properties.setKeyPrefix("lab:chat:session:");
        this.properties.setMaxMessages(4);
        this.properties.setTtlSeconds(100L);
        when(this.stringRedisTemplate.opsForList()).thenReturn(this.listOperations);
        this.store = new RedisConversationMessageStore(this.stringRedisTemplate, this.properties);
    }

    @Test
    void appendMessageShouldTrimAndExpire() {
        this.store.appendMessage("c1", "U:hello");

        verify(this.listOperations).rightPush("lab:chat:session:c1", "U:hello");
        verify(this.listOperations).trim("lab:chat:session:c1", -4, -1);
        verify(this.stringRedisTemplate).expire("lab:chat:session:c1", 100L, TimeUnit.SECONDS);
    }

    @Test
    void loadMessagesShouldReturnStoredValues() {
        when(this.listOperations.size("lab:chat:session:c2")).thenReturn(2L);
        when(this.listOperations.range("lab:chat:session:c2", 0, -1))
                .thenReturn(List.of("U:a", "A:b"));

        List<String> messages = this.store.loadMessages("c2");

        assertThat(messages).hasSize(2).containsExactly("U:a", "A:b");
    }

    @Test
    void loadMessagesShouldReturnEmptyWhenNoData() {
        when(this.listOperations.size(any())).thenReturn(0L);
        assertThat(this.store.loadMessages("c3")).isEmpty();
    }
}
