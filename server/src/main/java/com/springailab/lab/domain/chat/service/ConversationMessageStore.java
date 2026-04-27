package com.springailab.lab.domain.chat.service;

import java.util.List;

/**
 * 会话消息存储抽象。
 *
 * @author jiaolin
 */
public interface ConversationMessageStore {

    /**
     * 读取历史消息。
     *
     * @param conversationId 会话ID
     * @return 历史消息
     */
    List<String> loadMessages(String conversationId);

    /**
     * 追加消息。
     *
     * @param conversationId 会话ID
     * @param message 消息文本
     */
    void appendMessage(String conversationId, String message);
}
