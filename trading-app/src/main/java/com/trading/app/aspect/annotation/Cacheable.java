package com.trading.app.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 【職責】標記方法結果可被 {@link com.trading.app.aspect.CacheAspect} 快取。
 * 【技巧】自訂 RUNTIME 方法註解；切點用 {@code @annotation(cacheable)} 綁定。
 * 【概念】「宣告式快取」：業務方法只標註意圖，快取機制在切面。自幹簡化版是為了教學看懂 AOP，而非取代 Spring Cache。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {

    /**
     * 【職責】快取區名稱，區隔不同方法的鍵空間。
     * 【技巧】註解屬性預設 {@code "default"}。
     * 【概念】同名方法若在不同區，避免鍵碰撞。
     */
    String value() default "default";
}
