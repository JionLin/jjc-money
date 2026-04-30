package com.springailab.lab.web;

import com.springailab.lab.domain.chat.service.ChatOrchestrator;
import com.springailab.lab.domain.runtime.home.HomeRecommendationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 这是最外层的 Web Controller，也就是“浏览器 / 前端 调后端”时最先进入的 Java 类。
 *
 * 如果你以前没用过 Spring，可以把它理解成：
 * 1. 前端发 HTTP 请求到某个 URL
 * 2. Spring 根据注解，把请求分发到这个类里的某个方法
 * 3. 这个类自己通常不写复杂业务，只负责“接请求 -> 调 service -> 把结果返回”
 *
 * 这个类和 Spring AI 并没有强耦合。
 * 它只是整个 AI 对话链路的“入口门面”：
 * - `/chat`      返回一次性完整答案
 * - `/chat/stream` 返回流式答案（SSE）
 *
 * 所以阅读顺序可以这样理解：
 * ChatController -> ChatOrchestrator -> JinjianRuntimeService
 */
@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatOrchestrator chatOrchestrator;
    private final HomeRecommendationsService homeRecommendationsService;

    /**
     * 构造器注入依赖。
     *
     * 对刚接触 Spring 的人，可以这样理解：
     * - `ChatOrchestrator`：真正负责“聊天主流程”的人
     * - `HomeRecommendationsService`：负责首页推荐卡片的人
     *
     * `ChatController` 自己不做复杂计算，只是把请求转交给这些下游对象。
     * Spring 在启动项目时，会自动创建这些对象，并在这里传进来。
     */
    public ChatController(ChatOrchestrator chatOrchestrator,
                          HomeRecommendationsService homeRecommendationsService) {
        this.chatOrchestrator = chatOrchestrator;
        this.homeRecommendationsService = homeRecommendationsService;
    }

    /**
     * 首页推荐接口。
     *
     * `@GetMapping(...)` 表示：
     * - 这是一个 HTTP GET 接口
     * - URL 是 `/home/recommendations`
     * - 返回格式是 JSON
     *
     * 这个方法本身几乎没有逻辑，只做一件事：
     * 向 `homeRecommendationsService` 要一份首页推荐数据，然后直接返回。
     */
    @GetMapping(value = "/home/recommendations", produces = MediaType.APPLICATION_JSON_VALUE)
    public HomeRecommendationsService.HomeRecommendationsResponse homepageRecommendations() {
        return this.homeRecommendationsService.getHomepageRecommendations();
    }

    /**
     * 普通聊天接口，返回“一次性完整答案”。
     *
     * `@PostMapping(...)` 表示：
     * - 这是一个 HTTP POST 接口
     * - URL 是 `/chat`
     * - 前端要传 JSON 过来
     *
     * `@RequestBody ChatRequest request` 表示：
     * - Spring 会自动把前端传来的 JSON 反序列化成 `ChatRequest`
     * - 比如前端传 `{ "message": "...", "conversationId": "..." }`
     *   这里就能直接通过 `request.message()` / `request.conversationId()` 取值
     *
     * 这个方法的执行步骤非常简单：
     * 1. 打日志，方便排查问题
     * 2. 把 message / conversationId 交给 `ChatOrchestrator`
     * 3. 把编排层返回的完整结果作为 HTTP 响应返回给前端
     */
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {
        log.info("Chat request received, messageLength={}, request={}",
                request.message() == null ? 0 : request.message().length(), request);
        return this.chatOrchestrator.chat(request.message(), request.conversationId());
    }

    /**
     * 流式聊天接口，返回“边生成边推送”的答案。
     *
     * 这是理解整条链路的关键入口。
     *
     * `produces = MediaType.TEXT_EVENT_STREAM_VALUE` 表示：
     * - 这个接口不是一次性返回一整段 JSON
     * - 而是返回 SSE（Server-Sent Events）流
     * - 前端可以一边收，一边显示，形成“打字机效果”
     *
     * `SseEmitter` 可以先粗略理解为：
     * - 一个“持续往前端写数据”的通道对象
     * - 方法返回后，请求连接不会立刻结束
     * - 后端后续还能继续往这个连接里塞事件
     *
     * 这个 Controller 方法本身依然不处理 AI 生成逻辑，
     * 它只是把请求转给 `ChatOrchestrator.streamChat(...)`，
     * 真正的流式事件组装和发送都在下游完成。
     */
    @PostMapping(value = "/chat/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        log.info("Stream chat request received, messageLength={}, conversationId={}",
                request.message() == null ? 0 : request.message().length(), request.conversationId());
        return this.chatOrchestrator.streamChat(request.message(), request.conversationId());
    }

    /**
     * 这是聊天接口接收的请求体结构。
     *
     * Java `record` 可以理解成一个“专门装数据的轻量对象”。
     * 这里有两个字段：
     * - `message`：用户这一轮输入的内容
     * - `conversationId`：会话 ID，用来把多轮对话串起来
     *
     * 当前端 POST JSON 时，Spring 会把 JSON 自动映射成这个对象。
     */
    public record ChatRequest(String message, String conversationId) {
    }
}
