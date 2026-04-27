package com.springailab.lab.domain.chat.service;

import java.util.Collections;
import java.util.Map;

/**
 * SSE event payload.
 */
public class ChatEventPayload {

    private final String conversationId;

    private final String content;

    private final String type;

    private final Map<String, Object> metadata;

    public ChatEventPayload(String conversationId, String content) {
        this(conversationId, content, "token", Collections.emptyMap());
    }

    public ChatEventPayload(String conversationId, String content, String type, Map<String, Object> metadata) {
        this.conversationId = conversationId;
        this.content = content;
        this.type = type;
        this.metadata = metadata == null ? Collections.emptyMap() : metadata;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
