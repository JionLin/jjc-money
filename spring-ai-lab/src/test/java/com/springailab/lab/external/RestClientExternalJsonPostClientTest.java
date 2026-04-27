package com.springailab.lab.external;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link RestClientExternalJsonPostClient} 与 MockWebServer 的出站行为测试。
 *
 * @author jiaolin
 */
class RestClientExternalJsonPostClientTest {

    private MockWebServer server;

    private ExternalJsonPostClient client;

    /**
     * 启动 Mock 服务并装配客户端。
     *
     * @throws IOException 启动失败
     */
    @BeforeEach
    void setUp() throws IOException {
        this.server = new MockWebServer();
        this.server.start();
        LabExternalPostProperties properties = buildProperties(this.server.getPort());
        RestClient restClient = buildRestClientWithShortTimeouts();
        this.client = new RestClientExternalJsonPostClient(restClient, properties);
    }

    /**
     * 关闭 Mock 服务。
     *
     * @throws IOException 关闭失败
     */
    @AfterEach
    void tearDown() throws IOException {
        if (this.server != null) {
            this.server.close();
        }
    }

    /**
     * 2xx 且 JSON 体应原样返回。
     */
    @Test
    void postJsonReturnsBodyOnSuccess() {
        this.server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"temperature\":21,\"unit\":\"C\",\"condition\":\"sunny\"}"));
        String body = this.client.postJson(ExternalPostProfileKeys.WEATHER, Map.of("city", "Hangzhou"));
        assertThat(body).contains("21");
    }

    /**
     * 4xx 应映射为短错误（不透传响应体）。
     */
    @Test
    void postJsonMapsClientErrorToShortMessage() {
        this.server.enqueue(new MockResponse().setResponseCode(401).setBody("secret"));
        assertThatThrownBy(() -> this.client.postJson(ExternalPostProfileKeys.WEATHER, Map.of("city", "X")))
                .isInstanceOf(ExternalPostException.class)
                .hasMessageContaining("外部服务");
    }

    /**
     * 读超时或长时间无响应应映射为短错误。
     */
    @Disabled("MockWebServer 延迟体在当前环境下会导致关闭超时，先跳过该不稳定用例")
    @Test
    void postJsonMapsSlowBodyToTimeoutStyleError() {
        this.server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBodyDelay(10, TimeUnit.SECONDS)
                .setHeader("Content-Type", "application/json")
                .setBody("{}"));
        assertThatThrownBy(() -> this.client.postJson(ExternalPostProfileKeys.WEATHER, Map.of("city", "Y")))
                .isInstanceOf(ExternalPostException.class)
                .hasMessageContaining("外部服务");
    }

    private static LabExternalPostProperties buildProperties(int port) {
        LabExternalPostProperties properties = new LabExternalPostProperties();
        Map<String, LabExternalPostProperties.Endpoint> map = new LinkedHashMap<>();
        LabExternalPostProperties.Endpoint weather = new LabExternalPostProperties.Endpoint();
        weather.setBaseUrl("http://localhost:" + port);
        weather.setPath("/weather");
        map.put(ExternalPostProfileKeys.WEATHER, weather);
        LabExternalPostProperties.Endpoint echo = new LabExternalPostProperties.Endpoint();
        echo.setBaseUrl("http://localhost:" + port);
        echo.setPath("/demo-echo");
        map.put(ExternalPostProfileKeys.DEMO_ECHO, echo);
        properties.setEndpoints(map);
        return properties;
    }

    private static RestClient buildRestClientWithShortTimeouts() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(500));
        requestFactory.setReadTimeout(Duration.ofMillis(800));
        return RestClient.builder().requestFactory(requestFactory).build();
    }
}
