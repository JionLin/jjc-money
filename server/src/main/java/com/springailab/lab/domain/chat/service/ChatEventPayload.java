package com.springailab.lab.domain.chat.service;

/**
 * SSE 事件载荷。
 *
 * @author jiaolin
 */
public class ChatEventPayload {

    private final String conversationId;

    private final String content;

    public ChatEventPayload(String conversationId, String content) {
        this.conversationId = conversationId;
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getContent() {
        return content;
    }
}
