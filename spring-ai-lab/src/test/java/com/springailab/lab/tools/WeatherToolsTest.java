package com.springailab.lab.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springailab.lab.external.ExternalJsonPostClient;
import com.springailab.lab.external.ExternalPostProfileKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link WeatherTools} 摘要与多契约行为测试（出站由 mock 替代）。
 *
 * @author jiaolin
 */
@ExtendWith(MockitoExtension.class)
class WeatherToolsTest {

    @Mock
    private ExternalJsonPostClient externalJsonPostClient;

    private WeatherTools weatherTools;

    /**
     * 初始化被测对象。
     */
    @BeforeEach
    void setUp() {
        this.weatherTools = new WeatherTools(this.externalJsonPostClient, new ObjectMapper());
    }

    /**
     * 天气工具应压缩 JSON 为短摘要。
     */
    @Test
    void getWeatherSummarizesKeyFields() {
        when(this.externalJsonPostClient.postJson(eq(ExternalPostProfileKeys.WEATHER), any()))
                .thenReturn("{\"temperature\":10,\"unit\":\"C\",\"condition\":\"rain\"}");
        String text = this.weatherTools.getWeather("Hangzhou");
        assertThat(text).contains("10").contains("rain");
    }

    /**
     * 演示回显工具应使用另一契约字段摘要。
     */
    @Test
    void lookupDemoLineSummarizesEchoFields() {
        when(this.externalJsonPostClient.postJson(eq(ExternalPostProfileKeys.DEMO_ECHO), any()))
                .thenReturn("{\"message\":\"ok\",\"echoBody\":\"order-1\"}");
        String text = this.weatherTools.lookupDemoLine("order-1");
        assertThat(text).contains("ok").contains("order-1");
    }
}
