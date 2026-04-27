package com.springailab.lab;

import com.springailab.lab.domain.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * 上下文加载测试（不访问真实大模型；test profile 通过 include 加载 deepseek 占位配置）。
 *
 * @author jiaolin
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
@ActiveProfiles("test")
class SpringAiLabApplicationTests {

    @MockBean
    private SysUserMapper sysUserMapper;

    /**
     * 验证应用上下文可启动。
     */
    @Test
    void contextLoads() {
        // intentionally empty — context load is the assertion
    }
}
