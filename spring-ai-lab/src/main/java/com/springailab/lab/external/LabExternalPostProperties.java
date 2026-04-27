package com.springailab.lab.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 外部统一 POST JSON 端点配置（多 profile）。
 *
 * @author jiaolin
 */
@ConfigurationProperties(prefix = "lab.external.post")
public class LabExternalPostProperties {

    /**
     * profile 键 → 端点配置。
     */
    private Map<String, Endpoint> endpoints = new LinkedHashMap<>();

    /**
     * @return 端点映射
     */
    public Map<String, Endpoint> getEndpoints() {
        return this.endpoints;
    }

    /**
     * @param endpoints 端点映射
     */
    public void setEndpoints(Map<String, Endpoint> endpoints) {
        if (endpoints == null) {
            this.endpoints = new LinkedHashMap<>();
        } else {
            this.endpoints = endpoints;
        }
    }

    /**
     * 单个外部 HTTP 端点。
     */
    public static class Endpoint {

        private String baseUrl;

        private String path;

        private String apiKeyHeaderName;

        private String apiKey;

        /**
         * @return 基地址（不含 path）
         */
        public String getBaseUrl() {
            return this.baseUrl;
        }

        /**
         * @param baseUrl 基地址
         */
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         * @return 路径（以 / 开头）
         */
        public String getPath() {
            return this.path;
        }

        /**
         * @param path 路径
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * @return API Key 请求头名称（可选）
         */
        public String getApiKeyHeaderName() {
            return this.apiKeyHeaderName;
        }

        /**
         * @param apiKeyHeaderName 请求头名
         */
        public void setApiKeyHeaderName(String apiKeyHeaderName) {
            this.apiKeyHeaderName = apiKeyHeaderName;
        }

        /**
         * @return API Key 值（可选，来自配置）
         */
        public String getApiKey() {
            return this.apiKey;
        }

        /**
         * @param apiKey 密钥
         */
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
