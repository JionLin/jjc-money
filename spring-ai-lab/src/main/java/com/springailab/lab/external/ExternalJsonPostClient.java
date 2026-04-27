package com.springailab.lab.external;

/**
 * 向外部系统发送 POST {@code application/json} 的统一客户端。
 *
 * @author jiaolin
 */
public interface ExternalJsonPostClient {

    /**
     * 按 profile 将请求体序列化为 JSON 并 POST，返回响应体字符串。
     *
     * @param profileKey {@link LabExternalPostProperties#getEndpoints()} 中的键
     * @param body       请求体（由 Jackson 序列化）
     * @return 响应体原文
     * @throws ExternalPostException 配置缺失、HTTP 错误或超时
     */
    String postJson(String profileKey, Object body);
}
