package com.dongruan.environment.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author weiqiang
 * @version 1.0
 * @Date 2026/9/2 8:54
 */

//这个类就是swagger和核心配置类

@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("东软环境监测系统 API")
                        .description("API")
                        .version("1.0.0"));
    }
}
