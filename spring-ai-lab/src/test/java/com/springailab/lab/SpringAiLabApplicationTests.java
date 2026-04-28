package com.springailab.lab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 上下文加载测试（不访问真实大模型；test profile 通过 include 加载 deepseek 占位配置）。
 *
 * @author jiaolin
 */
@SpringBootTest
@ActiveProfiles("test")
class SpringAiLabApplicationTests {

    /**
     * 验证应用上下文可启动。
     */
    @Test
    void contextLoads() {
        // intentionally empty — context load is the assertion
    }
}
