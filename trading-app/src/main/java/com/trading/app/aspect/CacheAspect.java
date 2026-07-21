package com.trading.app.aspect;

import com.trading.app.aspect.annotation.Cacheable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 【職責】切面 5／6：對標註 {@link Cacheable} 的方法做簡易記憶體快取。
 * 【技巧】{@code @Around("@annotation(cacheable)")}；鍵＝區名+簽名+參數；命中則不 {@code proceed()}；{@code @Order(50)}。
 * 【概念】快取是橫切關注點。Around 可「短路」目標方法——這是 Before／After 做不到的。刻意自幹而非 Spring Cache，是為了把 AOP 實現攤開給初學者看。
 * 【邊界】無 TTL／容量淘汰；僅教學用。{@link #clear()} 供測試重置。
 */
@Aspect
@Component
@Order(50)
public class CacheAspect {

    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final AspectRecorder recorder;

    /**
     * 【職責】注入共用觀測器。
     * 【技巧】建構子注入。
     * 【概念】命中／未命中計數寫入 Recorder，證明快取生效。
     */
    public CacheAspect(AspectRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * 【職責】命中回快取；未命中執行方法並存入。
     * 【技巧】註解綁定；ConcurrentHashMap；短路時不呼叫 proceed。
     * 【概念】「不 proceed＝跳過業務本體」是 Around 最強大的能力之一（快取、權限拒絕、熔斷皆同模式）。
     */
    @Around("@annotation(cacheable)")
    public Object aroundCacheable(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String key = cacheable.value() + ":" + joinPoint.getSignature().toShortString()
                + ":" + Arrays.toString(joinPoint.getArgs());
        Object cached = cache.get(key);
        if (cached != null) {
            recorder.recordCacheHit();
            return cached;
        }
        Object result = joinPoint.proceed();
        cache.put(key, result);
        recorder.recordCacheMiss();
        return result;
    }

    /**
     * 【職責】清空快取（測試前重置）。
     * 【技巧】Map.clear。
     * 【概念】與 Recorder.clear 搭配，保證 CASE 隔離。
     */
    public void clear() {
        cache.clear();
    }
}
