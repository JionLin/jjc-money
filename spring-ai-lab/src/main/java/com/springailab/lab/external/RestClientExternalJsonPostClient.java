package com.springailab.lab.external;

import com.springailab.lab.web.TraceIdFilter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * 基于 {@link RestClient} 的 {@link ExternalJsonPostClient} 实现。
 *
 * @author jiaolin
 */
public class RestClientExternalJsonPostClient implements ExternalJsonPostClient {

    private static final Logger log = LoggerFactory.getLogger(RestClientExternalJsonPostClient.class);

    private final RestClient restClient;

    private final LabExternalPostProperties properties;

    /**
     * @param restClient 出站 RestClient（已配置超时）
     * @param properties 端点配置
     */
    public RestClientExternalJsonPostClient(RestClient restClient, LabExternalPostProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    @Retry(name = "externalPost", fallbackMethod = "fallbackPostJson")
    @CircuitBreaker(name = "externalPost", fallbackMethod = "fallbackPostJson")
    public String postJson(String profileKey, Object body) {
        LabExternalPostProperties.Endpoint endpoint = resolveEndpoint(profileKey);
        String url = buildUrl(endpoint);
        if (log.isDebugEnabled()) {
            log.debug("POST JSON profile={}, url={}", profileKey, url);
        }
        try {
            return this.restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> applyHeaders(endpoint, headers))
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new ExternalPostException("外部服务暂时不可用", null);
                    })
                    .body(String.class);
        } catch (ExternalPostException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw new ExternalPostException("外部服务连接超时或不可用", ex);
        } catch (Exception ex) {
            throw new ExternalPostException("外部服务调用失败", ex);
        }
    }

    private static void applyHeaders(LabExternalPostProperties.Endpoint endpoint,
            org.springframework.http.HttpHeaders headers) {
        String traceId = MDC.get("traceId");
        if (StringUtils.hasText(traceId)) {
            headers.add(TraceIdFilter.TRACE_ID_HEADER, traceId);
        }
        if (!StringUtils.hasText(endpoint.getApiKeyHeaderName()) || !StringUtils.hasText(endpoint.getApiKey())) {
            return;
        }
        headers.add(endpoint.getApiKeyHeaderName(), endpoint.getApiKey());
    }

    /**
     * 重试/熔断失败后的统一降级处理。
     *
     * @param profileKey 请求 profile
     * @param body       请求体
     * @param throwable  原始异常
     * @return 不返回正常内容，直接抛统一异常
     */
    @SuppressWarnings("unused")
    private String fallbackPostJson(String profileKey, Object body, Throwable throwable) {
        throw new ExternalPostException("外部服务熔断降级", throwable);
    }

    private LabExternalPostProperties.Endpoint resolveEndpoint(String profileKey) {
        if (!StringUtils.hasText(profileKey)) {
            throw new ExternalPostException("外部服务未配置", null);
        }
        LabExternalPostProperties.Endpoint endpoint = this.properties.getEndpoints().get(profileKey);
        if (endpoint == null) {
            throw new ExternalPostException("外部服务未配置", null);
        }
        if (!StringUtils.hasText(endpoint.getBaseUrl()) || !StringUtils.hasText(endpoint.getPath())) {
            throw new ExternalPostException("外部服务未配置", null);
        }
        return endpoint;
    }

    private static String buildUrl(LabExternalPostProperties.Endpoint endpoint) {
        String base = endpoint.getBaseUrl().trim();
        String path = endpoint.getPath().trim();
        if (base.endsWith("/") && path.startsWith("/")) {
            return base.substring(0, base.length() - 1) + path;
        }
        if (!base.endsWith("/") && !path.startsWith("/")) {
            return base + "/" + path;
        }
        return base + path;
    }
}
