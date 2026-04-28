package com.springailab.lab.domain.chat.service;

import com.springailab.lab.domain.chat.config.ChatSessionProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory conversation store used when external cache is not enabled.
 */
@Service
public class InMemoryConversationMessageStore implements ConversationMessageStore {

    private final ChatSessionProperties properties;

    private final ConcurrentMap<String, List<String>> store = new ConcurrentHashMap<>();

    public InMemoryConversationMessageStore(ChatSessionProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<String> loadMessages(String conversationId) {
        String key = normalizeConversationId(conversationId);
        List<String> messages = this.store.getOrDefault(key, List.of());
        return new ArrayList<>(messages);
    }

    @Override
    public void appendMessage(String conversationId, String message) {
        String key = normalizeConversationId(conversationId);
        this.store.compute(key, (k, existing) -> {
            List<String> messages = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            messages.add(message);
            int max = Math.max(this.properties.getMaxMessages(), 1);
            if (messages.size() > max) {
                messages = new ArrayList<>(messages.subList(messages.size() - max, messages.size()));
            }
            return messages;
        });
    }

    private static String normalizeConversationId(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return "default";
        }
        return conversationId.trim();
    }
}
