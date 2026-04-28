package com.springailab.lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI + DeepSeek（OpenAI 兼容）最小示例入口。
 *
 * @author jiaolin
 */
@SpringBootApplication
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
