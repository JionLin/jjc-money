package com.springailab.lab.domain.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 大模型估算成本配置。
 *
 * @author jiaolin
 */
@Component
@ConfigurationProperties(prefix = "lab.cost.price-per-1k-tokens")
public class ChatCostProperties {

    private BigDecimal chat = BigDecimal.ZERO;

    private BigDecimal embedding = BigDecimal.ZERO;

    public BigDecimal getChat() {
        return chat;
    }

    public void setChat(BigDecimal chat) {
        this.chat = chat;
    }

    public BigDecimal getEmbedding() {
        return embedding;
    }

    public void setEmbedding(BigDecimal embedding) {
        this.embedding = embedding;
    }
}
