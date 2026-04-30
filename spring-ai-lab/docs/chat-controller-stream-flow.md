# ChatController streamChat 链路深度拆解

流式聊天接口的逻辑较长，我们将其拆分为：**一个总览 + 三个核心阶段**。

---

## 0. 总览：三大角色分工
在看具体代码前，先记住这三个角色的职责，这是理解整条链路的基石。

```mermaid
graph TD
    Client(前端/浏览器) -- 1. 发起 POST 请求 --> Controller[ChatController <br/> 负责接客, 快速响应]
    Controller -- 2. 调度异步任务 --> Orchestrator[ChatOrchestrator <br/> 负责流程控制, 吐出 SSE 事件]
    Orchestrator -- 3. 执行核心逻辑 --> Runtime[JinjianRuntimeService <br/> 负责查归档, 拿数据, 拼答案]
    Runtime -. 4. 返回完整答案 .-> Orchestrator
    Orchestrator -. 5. 推送流式分片 .-> Client
```

---

## 1. 第一阶段：请求接管与异步握手
**重点**：为什么 AI 思考很久，你的 Web 页面却不会卡死？
因为 Controller 把活儿扔给异步线程后就立刻“下班”了。

```mermaid
sequenceDiagram
    participant Client as 浏览器
    participant Controller as ChatController
    participant Orchestrator as ChatOrchestrator
    
    Client->>Controller: "POST /chat/stream"
    Controller->>Orchestrator: "streamChat(message)"
    
    Note right of Orchestrator: "创建 SseEmitter (建立管道)"
    Orchestrator-->>Controller: "立刻返回 Emitter 对象"
    Controller-->>Client: "200 OK (Content-Type: text/event-stream)"
    
    Note over Client,Controller: "此时 HTTP 连接已建立，Controller 线程已释放"
```

---

## 2. 第二阶段：AI 运行时核心逻辑 (The Brain)
**重点**：这一步在 `JinjianRuntimeService` 内部执行，是真正的“思考”过程。

```mermaid
sequenceDiagram
    participant Orchestrator as ChatOrchestrator
    participant Runtime as JinjianRuntimeService
    participant Router as ModeRouter
    participant Evidence as 数据源(归档/实时)
    
    Note over Orchestrator,Runtime: "异步线程内部开始干活"
    
    Orchestrator->>Runtime: "execute(message)"
    Runtime->>Router: "route: 识别问题类型 (Ticker/Historical)"
    Router-->>Runtime: "返回决策结果"
    
    alt Ticker 模式 (需实时数据)
        Runtime->>Evidence: "查历史归档 + 查 Fresh Facts"
    else Historical 模式 (仅查历史)
        Runtime->>Evidence: "仅查历史归档"
    end
    
    Evidence-->>Runtime: "返回所有原始材料"
    Note over Runtime: "拼装最终回复文本 <br/> 并切分成多个分片事件"
    Runtime-->>Orchestrator: "返回包含所有事件的 RuntimeAnswer"
```

---

## 3. 第三阶段：流式事件推送
**重点**：答案是怎么像打字机一样出来的？
Orchestrator 遍历 Runtime 准备好的事件，一个个塞进第一阶段建好的“管道”里。

```mermaid
sequenceDiagram
    participant Client as 浏览器
    participant Orchestrator as ChatOrchestrator
    
    Note over Orchestrator: "拿到 RuntimeAnswer 后"
    
    loop 遍历每一条内部事件 (StreamEvent)
        Orchestrator->>Client: "emitter.send(mode_detected)"
        Orchestrator->>Client: "emitter.send(source_cited)"
        Orchestrator->>Client: "emitter.send(answer_chunk 1...N)"
    end
    
    Orchestrator->>Client: "发送 done 事件 (告知彻底结束)"
    Orchestrator->>Client: "emitter.complete() (关闭管道)"
```

---

## 总结：如果你要调试代码...
- **前端收不到任何返回**：查 `ChatController`。
- **SSE 连接秒断或超时**：查 `ChatOrchestrator` 的超时设置。
- **回答内容不对/没引用证据**：查 `JinjianRuntimeService` 和 `ModeRouter`。
- **打字机效果不流畅**：查 `JinjianRuntimeService` 里的文本切片逻辑。
