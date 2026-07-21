package com.trading.miniioc.aop;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 【職責】計算方法耗時（對應 trading-app {@code PerformanceAspect}）。
 * 【技巧】Around + {@code finally} 計時（成功／失敗都記錄）；{@link System#nanoTime} 轉毫秒。
 * 【概念】效能觀測與業務邏輯解耦；用 {@code finally} 確保例外路徑也量得到——這是 Around 相對單純 Before／After 的優勢之一。
 */
public class TimingInterceptor implements MethodInterceptor {

    private final Map<String, Long> timings = new LinkedHashMap<>();

    /**
     * 【職責】量測 {@code proceed()} 耗時並寫入 {@link #getTimings()}。
     * 【技巧】nanoTime 差值；方法名當 key。
     * 【概念】教學對照：同一「計時」關注點，手刻攔截器 vs Spring {@code @Around} 只是掛載方式不同。
     */
    @Override
    public Object invoke(Invocation invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        long start = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            timings.put(method, elapsedMs);
            System.out.println("[TIMING] " + method + " 耗時 " + elapsedMs + " ms");
        }
    }

    /** 各方法最近一次耗時（毫秒）。 */
    public Map<String, Long> getTimings() {
        return timings;
    }
}
