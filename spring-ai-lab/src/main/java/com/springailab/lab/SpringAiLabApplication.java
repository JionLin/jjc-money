package com.springailab.lab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Spring AI + DeepSeek（OpenAI 兼容）最小示例入口。
 *
 * @author jiaolin
 */
@SpringBootApplication
@MapperScan("com.springailab.lab.domain.user.mapper")
public class SpringAiLabApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(SpringAiLabApplication.class);



        app.run(args);
    }


}
