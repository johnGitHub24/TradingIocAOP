package com.trading.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 【職責】註冊 OpenAPI（Swagger UI）文件 bean。
 * 【技巧】{@code @Configuration} + {@code @Bean} 工廠式登記。
 * 【概念】IoC 的另一種註冊方式：不一定要 {@code @Component} 掃描——{@code @Bean} 方法回傳的物件也由容器管理。對照 mini-ioc 只有掃描／register。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 【職責】提供 API 文件標題與說明。
     * 【技巧】{@code @Bean} 方法；回傳 {@link OpenAPI}。
     * 【概念】組態類集中「基礎設施 bean」，與業務 {@code @Service} 分離。
     */
    @Bean
    public OpenAPI tradingIocAopOpenApi() {
        return new OpenAPI().info(new Info()
                .title("TradingIocAOP API")
                .version("0.1.0")
                .description("示範 依賴注入 / 控制反轉 / AOP 的交易下單服務"));
    }
}
