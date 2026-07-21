package com.trading.app.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 【職責】標記方法失敗時由 {@link com.trading.app.aspect.RetryAspect} 自動重試。
 * 【技巧】自訂 RUNTIME 方法註解；{@link #maxAttempts()} 傳入切面。
 * 【概念】重試策略與業務邏輯分離：貼標＝加入橫切行為，方法本體保持「送一次」的語意。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retryable {

    /**
     * 【職責】最大嘗試次數（含第一次），預設 3。
     * 【技巧】註解 int 屬性。
     * 【概念】含首次：maxAttempts=3 表示最多執行 3 次，不是「再試 3 次」。
     */
    int maxAttempts() default 3;
}
