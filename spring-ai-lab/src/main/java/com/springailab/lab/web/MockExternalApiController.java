package com.springailab.lab.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 本地联调用的外部 API Mock，仅在 mock profile 下生效。
 *
 * @author jiaolin
 */
@RestController
@RequestMapping(value = "/mock", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class MockExternalApiController {

    /**
     * mock 天气查询。
     *
     * @param request 入参
     * @return 固定天气结果
     */
    @PostMapping("/weather")
    public Map<String, Object> weather(@RequestBody WeatherMockRequest request) {
        return Map.of(
                "temperature", 24,
                "unit", "C",
                "condition", "sunny",
                "city", request.city());
    }

    /**
     * mock 回显查询。
     *
     * @param request 入参
     * @return 固定回显结果
     */
    @PostMapping("/demo-echo")
    public Map<String, String> demoEcho(@RequestBody DemoEchoMockRequest request) {
        return Map.of(
                "message", "ok",
                "echoBody", request.query());
    }

    /**
     * @param city 城市名
     */
    public record WeatherMockRequest(String city) {
    }

    /**
     * @param query 查询词
     */
    public record DemoEchoMockRequest(String query) {
    }
}
