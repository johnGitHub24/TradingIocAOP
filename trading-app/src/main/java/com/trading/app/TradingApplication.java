package com.trading.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 【職責】trading-app 進入點，啟動 Spring Boot 與 IoC 容器。
 * 【技巧】{@code @SpringBootApplication}（自動組態 + 元件掃描 + 屬性繫結）；{@link SpringApplication#run}。
 * 【概念】啟動後的 ApplicationContext 就是工業級 IoC 容器——對照 mini-ioc 的 {@code MiniApplicationContext}，兩者都在「建立 bean、注入相依」；Spring 額外內建 AOP 代理織入。
 */
@SpringBootApplication
public class TradingApplication {

    /**
     * 【職責】啟動應用程式。
     * 【技巧】將本類作為 primary source 交給 Spring Boot。
     * 【概念】main 幾乎空白是刻意的：組態與 bean 生命週期交給容器，而非手寫組裝。
     */
    public static void main(String[] args) {
        SpringApplication.run(TradingApplication.class, args);
    }
}
