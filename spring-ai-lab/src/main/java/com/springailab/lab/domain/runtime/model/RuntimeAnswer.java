package com.springailab.lab.domain.runtime.model;

import com.springailab.lab.domain.runtime.trace.RuntimeTrace;

import java.util.List;

/**
 * 运行时的完整结果对象。
 *
 * 它不是只有一段回复文本，而是把这次 AI 执行的关键产物都打包在一起：
 * - `reply`：最终完整回答
 * - `mode`：本次命中的模式
 * - `citations`：引用到的证据
 * - `freshFact`：命中的实时数据
 * - `degradeStatus`：是否降级
 * - `disclaimer`：统一风险提示
 * - `trace`：本次执行的追踪信息
 * - `streamEvents`：供流式接口直接使用的事件列表
 */
public record RuntimeAnswer(String reply,
                            RuntimeMode mode,
                            List<CitationRecord> citations,
                            FreshFactRecord freshFact,
                            DegradeStatus degradeStatus,
                            String disclaimer,
                            RuntimeTrace trace,
                            List<RuntimeStreamEvent> streamEvents) {
}
