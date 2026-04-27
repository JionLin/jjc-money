package com.springailab.lab.domain.chat.service;

import com.springailab.lab.domain.chat.config.ChatCostProperties;
import com.springailab.lab.tools.WeatherTools;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 对话编排服务（同步 + SSE + 会话记忆 + 指标）。
 *
 * @author jiaolin
 */
@Service
public class ChatOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestrator.class);

    private static final Duration STREAM_TIMEOUT = Duration.ofMinutes(5);

    private static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000L);

    private final ChatClient chatClient;

    private final WeatherTools weatherTools;

    private final ConversationMessageStore conversationMessageStore;

    private final MeterRegistry meterRegistry;

    private final ChatCostProperties chatCostProperties;

    public ChatOrchestrator(ChatModel chatModel,
            WeatherTools weatherTools,
            ConversationMessageStore conversationMessageStore,
            MeterRegistry meterRegistry,
            ChatCostProperties chatCostProperties) {
        // 基于注入的聊天模型创建 ChatClient 实例。
        this.chatClient = ChatClient.builder(chatModel).build();
        // 保存天气工具实例，供大模型在对话中调用。
        this.weatherTools = weatherTools;
        // 保存会话消息存储组件，用于读取和写入历史消息。
        this.conversationMessageStore = conversationMessageStore;
        // 保存指标注册中心，用于记录监控指标。
        this.meterRegistry = meterRegistry;
        // 保存成本配置，用于后续估算 AI 调用费用。
        this.chatCostProperties = chatCostProperties;
    }

    /**
     * 同步对话。
     *
     * @param message 用户消息
     * @param conversationId 会话ID
     * @return HTTP 响应
     */
    public ResponseEntity<Map<String, String>> chat(String message, String conversationId) {
        // 规范化会话 ID，保证后续处理一定有可用值。
        String normalizedConversationId = normalizeConversationId(conversationId);
        // 规范化用户输入消息，避免空消息进入模型。
        String normalizedMessage = normalizeMessage(message);
        try {
            // 调用大模型生成本次同步对话响应。
            ChatResponse response = this.chatClient.prompt()
                    // 组装历史消息和当前用户消息作为提示词。
                    .messages(buildPromptMessages(normalizedConversationId, normalizedMessage))
                    // 注册天气工具，允许模型按需调用。
                    .tools(this.weatherTools)
                    // 发起同步调用。
                    .call()
                    // 取得底层聊天响应对象。
                    .chatResponse();
            // 从响应中提取模型输出文本。
            String content = extractContent(response);
            // 将本轮用户消息和助手回复写入会话历史。
            appendConversation(normalizedConversationId, normalizedMessage, content);
            // 根据 token 使用情况采集本次同步调用的预估成本。
            collectEstimatedCostFromUsage(response, "chat", "false");
            // 返回成功响应，并把回复内容放入 reply 字段。
            return ResponseEntity.ok(Map.of("reply", content));
        } catch (NonTransientAiException ex) {
            // 读取异常消息，便于日志和错误响应复用。
            String msg = ex.getMessage();
            // 记录不可重试的 AI 调用异常日志。
            log.error("AI 调用失败（不可重试）: {}", msg, ex);
            // 如果异常中包含 401，说明 API Key 无效或未配置。
            if (msg != null && msg.contains("401")) {
                // 返回 401 状态码，并提示用户检查 API Key 配置。
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        // 返回认证失败的详细错误信息。
                        .body(Map.of("error", "API Key 无效或未配置，请检查环境变量 DASHSCOPE_API_KEY 是否正确设置。",
                                "detail", "https://help.aliyun.com/zh/model-studio/error-code#apikey-error"));
            }
            // 其他异常统一返回 500，并附带错误详情。
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    // 返回通用 AI 服务调用失败信息。
                    .body(Map.of("error", "AI 服务调用失败", "detail", msg));
        }
    }

    /**
     * SSE 流式对话。
     *
     * @param message 用户消息
     * @param conversationId 会话ID
     * @return SSE 发射器
     */
    public SseEmitter streamChat(String message, String conversationId) {
        // 规范化会话 ID，保证流式对话关联到有效会话。
        String normalizedConversationId = normalizeConversationId(conversationId);
        // 规范化用户输入消息，避免空内容进入模型。
        String normalizedMessage = normalizeMessage(message);
        // 创建 SSE 发射器，并设置超时时间。
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT.toMillis());
        // 记录本次流式请求的开始时间样本。
        Timer.Sample sample = Timer.start(this.meterRegistry);
        // 构建流式请求总数指标并注册。
        Counter.builder("chat_stream_requests_total")
                // 将指标注册到监控中心。
                .register(this.meterRegistry)
                // 将流式请求总数加一。
                .increment();

        // 标记流式请求是否已经结束，防止重复收尾。
        AtomicBoolean finished = new AtomicBoolean(false);
        // 保存最新一次响应中的 usage 信息，用于结束时统计成本。
        AtomicReference<Usage> latestUsage = new AtomicReference<>();
        // 累积流式返回的文本片段，最终形成完整回复。
        StringBuilder streamedContent = new StringBuilder();
        // 创建流式响应 Flux，用于持续接收模型输出。
        Flux<ChatResponse> responseFlux = this.chatClient.prompt()
                // 组装历史消息和本次用户消息。
                .messages(buildPromptMessages(normalizedConversationId, normalizedMessage))
                // 注册天气工具，允许模型在流式过程中调用。
                .tools(this.weatherTools)
                // 发起流式调用。
                .stream()
                // 获取底层流式聊天响应对象。
                .chatResponse();
        // 订阅流式响应，处理 token、异常和完成事件。
        Disposable disposable = responseFlux.subscribe(chatResponse -> {
            // 尝试提取当前片段附带的 usage 信息。
            Usage usage = extractUsage(chatResponse);
            // 如果本次片段包含 usage，则刷新最新 usage。
            if (usage != null) {
                // 保存最新 usage，供流结束时使用。
                latestUsage.set(usage);
            }
            // 提取当前流式片段中的文本内容。
            String token = extractContent(chatResponse);
            // 如果当前片段没有可用文本，则直接忽略。
            if (!StringUtils.hasText(token)) {
                // 没有 token 时提前结束本次片段处理。
                return;
            }
            // 将当前 token 追加到完整回复缓冲区。
            streamedContent.append(token);
            // 通过 SSE 将当前 token 推送给前端。
            sendEvent(emitter, "token", new ChatEventPayload(normalizedConversationId, token));
        }, throwable -> {
            // 流式调用异常时，增加错误计数指标。
            Counter.builder("chat_stream_errors_total")
                    // 将错误指标注册到监控中心。
                    .register(this.meterRegistry)
                    // 将错误次数加一。
                    .increment();
            // 向前端发送 error 事件，告知流式调用失败。
            sendEvent(emitter, "error", new ChatEventPayload(normalizedConversationId, "AI 服务调用失败"));
            // 执行流式收尾逻辑，记录耗时和成本。
            finalizeStream(finished, sample, latestUsage.get(), "true");
            // 以异常方式结束 SSE 连接。
            emitter.completeWithError(throwable);
        }, () -> {
            // 正常完成时向前端发送 done 事件。
            sendEvent(emitter, "done", new ChatEventPayload(normalizedConversationId, "completed"));
            // 将完整的用户消息和模型回复写入会话历史。
            appendConversation(normalizedConversationId, normalizedMessage, streamedContent.toString());
            // 执行流式收尾逻辑，记录耗时和成本。
            finalizeStream(finished, sample, latestUsage.get(), "true");
            // 正常结束 SSE 连接。
            emitter.complete();
        });

        // 当连接正常关闭时，释放流式订阅资源。
        emitter.onCompletion(disposable::dispose);
        // 注册超时处理逻辑。
        emitter.onTimeout(() -> {
            // 超时时主动释放流式订阅资源。
            disposable.dispose();
            // 仅在首次结束时执行超时收尾逻辑。
            if (finished.compareAndSet(false, true)) {
                // 记录一次流式超时/异常计数。
                Counter.builder("chat_stream_errors_total")
                        // 将错误指标注册到监控中心。
                        .register(this.meterRegistry)
                        // 将错误次数加一。
                        .increment();
                // 停止耗时统计并记录流式调用时延。
                sample.stop(Timer.builder("chat_stream_latency").register(this.meterRegistry));
            }
            // 超时后结束 SSE 连接。
            emitter.complete();
        });
        // 返回 SSE 发射器给调用方。
        return emitter;
    }

    private void finalizeStream(AtomicBoolean finished, Timer.Sample sample, Usage usage, String toolInvoked) {
        // 如果已经执行过收尾，则直接返回避免重复处理。
        if (!finished.compareAndSet(false, true)) {
            // 已完成收尾时不再重复记录指标。
            return;
        }
        // 记录本次流式调用的耗时指标。
        sample.stop(Timer.builder("chat_stream_latency").register(this.meterRegistry));
        // 根据 usage 信息记录本次调用的预估成本。
        collectEstimatedCost(usage, "chat", toolInvoked);
    }

    private void collectEstimatedCostFromUsage(ChatResponse response, String callType, String toolInvoked) {
        // 从响应中提取 usage 后继续走统一的成本统计逻辑。
        collectEstimatedCost(extractUsage(response), callType, toolInvoked);
    }

    private void collectEstimatedCost(Usage usage, String callType, String toolInvoked) {
        // 如果 usage 或 token 信息缺失，则无法估算成本，直接返回。
        if (usage == null || usage.getPromptTokens() == null || usage.getCompletionTokens() == null) {
            // 缺少统计数据时跳过成本采集。
            return;
        }
        // 计算输入和输出 token 的总数。
        BigDecimal tokens = BigDecimal.valueOf(usage.getPromptTokens() + usage.getCompletionTokens());
        // 根据调用类型获取对应的单价配置。
        BigDecimal unitPrice = resolveUnitPrice(callType);
        // 如果单价小于等于 0，则不记录成本。
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            // 无有效单价时直接结束。
            return;
        }
        // 将 token 数量换算为千 token 单位后乘以单价，得到预估成本。
        BigDecimal estimatedCost = tokens
                // 将 token 总数按每千 token 换算并保留 6 位小数。
                .divide(ONE_THOUSAND, 6, RoundingMode.HALF_UP)
                // 乘以单价得到最终预估费用。
                .multiply(unitPrice);
        // 构建预估成本指标。
        Counter.builder("ai_estimated_cost_total")
                // 标记调用类型维度。
                .tag("call_type", callType)
                // 标记是否调用工具维度。
                .tag("tool_invoked", toolInvoked)
                // 注册到指标中心。
                .register(this.meterRegistry)
                // 将预估成本累加到计数器中。
                .increment(estimatedCost.doubleValue());
    }

    private BigDecimal resolveUnitPrice(String callType) {
        // 如果是向量化调用，则返回 embedding 单价。
        if ("embedding".equals(callType)) {
            // 读取 embedding 的价格配置。
            return this.chatCostProperties.getEmbedding();
        }
        // 默认返回聊天调用的价格配置。
        return this.chatCostProperties.getChat();
    }

    private List<Message> buildPromptMessages(String conversationId, String message) {
        // 从会话存储中加载历史消息记录。
        List<String> history = this.conversationMessageStore.loadMessages(conversationId);
        // 创建消息列表，用于传递给模型。
        List<Message> messages = new ArrayList<>();
        // 遍历历史消息，恢复为模型可识别的消息对象。
        for (String item : history) {
            // 如果是用户消息前缀，则恢复为 UserMessage。
            if (item.startsWith("U:")) {
                // 去掉前缀后加入用户消息对象。
                messages.add(new UserMessage(item.substring(2)));
                // 继续处理下一条历史消息。
                continue;
            }
            // 如果是助手消息前缀，则恢复为 AssistantMessage。
            if (item.startsWith("A:")) {
                // 去掉前缀后加入助手消息对象。
                messages.add(new AssistantMessage(item.substring(2)));
            }
        }
        // 将本次用户输入追加到消息列表末尾。
        messages.add(new UserMessage(message));
        // 返回完整提示消息列表。
        return messages;
    }

    private void appendConversation(String conversationId, String userMessage, String assistantReply) {
        // 将用户消息按约定前缀写入会话存储。
        this.conversationMessageStore.appendMessage(conversationId, "U:" + userMessage);
        // 将助手回复按约定前缀写入会话存储。
        this.conversationMessageStore.appendMessage(conversationId, "A:" + assistantReply);
    }

    private static String normalizeConversationId(String conversationId) {
        // 如果传入的会话 ID 有内容，则直接返回去除首尾空格后的值。
        if (StringUtils.hasText(conversationId)) {
            // 返回清洗后的会话 ID。
            return conversationId.trim();
        }
        // 如果未传会话 ID，则自动生成一个新的会话标识。
        return "conversation-" + UUID.randomUUID();
    }

    private static String normalizeMessage(String message) {
        // 如果用户消息为空，则提供默认问候语。
        if (!StringUtils.hasText(message)) {
            // 返回默认消息内容。
            return "你好";
        }
        // 返回去除首尾空格后的用户消息。
        return message.trim();
    }

    private static String extractContent(ChatResponse response) {
        // 如果响应、结果或输出为空，则返回空字符串。
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            // 无法提取文本时返回空字符串。
            return "";
        }
        // 从响应输出中读取文本内容。
        String text = response.getResult().getOutput().getText();
        // 如果文本本身为空，则返回空字符串。
        if (text == null) {
            // 文本为空时返回默认空值。
            return "";
        }
        // 返回提取出的文本内容。
        return text;
    }

    private static Usage extractUsage(ChatResponse response) {
        // 如果响应或元数据为空，则说明没有 usage 信息。
        if (response == null || response.getMetadata() == null) {
            // 无 usage 时返回 null。
            return null;
        }
        // 返回响应元数据中的 usage 信息。
        return response.getMetadata().getUsage();
    }

    private static void sendEvent(SseEmitter emitter, String eventName, ChatEventPayload payload) {
        try {
            // 发送一个带事件名和数据体的 SSE 事件。
            emitter.send(SseEmitter.event()
                    // 设置 SSE 事件名称。
                    .name(eventName)
                    // 设置 SSE 事件数据。
                    .data(payload));
        } catch (IOException ex) {
            // 发送失败时转换为运行时异常抛出。
            throw new IllegalStateException("SSE send failed", ex);
        }
    }
}
