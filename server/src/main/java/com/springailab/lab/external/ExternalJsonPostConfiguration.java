package com.springailab.lab.external;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * 外部 POST JSON 客户端与配置注册。
 *
 * @author jiaolin
 */
@Configuration
@EnableConfigurationProperties(LabExternalPostProperties.class)
public class ExternalJsonPostConfiguration {

    private static final int CONNECT_TIMEOUT_MILLIS = 2_000;

    private static final int READ_TIMEOUT_MILLIS = 5_000;

    /**
     * 出站专用 {@link RestClient}（与 MVC 用例隔离）。
     *
     * @return RestClient
     */
    @Bean
    public RestClient labExternalRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MILLIS));
        requestFactory.setReadTimeout(Duration.ofMillis(READ_TIMEOUT_MILLIS));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * @param restClient   出站客户端
     * @param properties   端点配置
     * @return {@link ExternalJsonPostClient}
     */
    @Bean
    public ExternalJsonPostClient externalJsonPostClient(RestClient labExternalRestClient,
            LabExternalPostProperties properties) {
        return new RestClientExternalJsonPostClient(labExternalRestClient, properties);
    }
}
