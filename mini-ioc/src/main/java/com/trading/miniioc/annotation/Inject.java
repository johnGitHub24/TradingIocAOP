package com.trading.miniioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 【職責】標記容器應使用的建構子，等同 Spring 建構子注入時的 {@code @Autowired}。
 * 【技巧】{@code @Target(CONSTRUCTOR)} + RUNTIME；容器用 {@code isAnnotationPresent} 挑選。
 * 【概念】「多建構子時誰負責注入」必須明確——單一建構子可省略本註解（慣例推斷）；多建構子若不標註，容器無法猜，應失敗而非 silently 選錯。
 * 【邊界】本教學只支援建構子注入，不做 field／setter 注入。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface Inject {
}
