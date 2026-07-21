package com.trading.app.aspect;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 【職責】集中收集六大切面的觀測結果，讓 AOP 效果「看得見、測得到」。
 * 【技巧】Spring {@code @Component} 單例；執行緒安全集合（CopyOnWrite／Concurrent／Atomic）。
 * 【概念】切面若只打 log，測試難斷言。Recorder 是「觀測匯流排」：切面寫入、整合測試與 {@code /api/v1/aspects/report} 讀出——把橫切行為變成可驗證資料。
 * 【邊界】不實作切面邏輯本身；只做記錄與清空。
 */
@Component
public class AspectRecorder {

    private final List<String> logs = new CopyOnWriteArrayList<>();
    private final Map<String, Long> timings = new ConcurrentHashMap<>();
    private final List<String> audits = new CopyOnWriteArrayList<>();
    private final List<String> exceptions = new CopyOnWriteArrayList<>();
    private final AtomicInteger cacheHits = new AtomicInteger();
    private final AtomicInteger cacheMisses = new AtomicInteger();
    private final Map<String, Integer> retryAttempts = new ConcurrentHashMap<>();

    /**
     * 【職責】記錄 LoggingAspect 進出訊息。
     * 【技巧】追加到執行緒安全 List。
     * 【概念】把「發生過什麼」留下證據，供測試與報表使用。
     */
    public void recordLog(String line) {
        logs.add(line);
    }

    /**
     * 【職責】記錄 PerformanceAspect 耗時（毫秒）。
     * 【技巧】以方法簽名字串為 key 覆寫最近一次。
     * 【概念】教學只需「有量到」，不追求百分位統計。
     */
    public void recordTiming(String method, long elapsedMs) {
        timings.put(method, elapsedMs);
    }

    /**
     * 【職責】記錄 AuditAspect 稽核列。
     * 【技巧】追加字串列。
     * 【概念】稽核是合規橫切關注點，與業務成交邏輯分離存放。
     */
    public void recordAudit(String line) {
        audits.add(line);
    }

    /**
     * 【職責】記錄 ExceptionAspect 攔截到的例外摘要。
     * 【技巧】追加字串列。
     * 【概念】證明「例外被觀測到」但不被吞掉——與 GlobalExceptionHandler 分工。
     */
    public void recordException(String line) {
        exceptions.add(line);
    }

    /**
     * 【職責】快取命中計數 +1。
     * 【技巧】{@link AtomicInteger#incrementAndGet}。
     * 【概念】命中／未命中分開計，才能證明 CacheAspect 生效。
     */
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    /**
     * 【職責】快取未命中計數 +1。
     * 【技巧】原子遞增。
     * 【概念】第一次呼叫應 miss，第二次同 key 應 hit。
     */
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    /**
     * 【職責】記錄某方法的重試嘗試次數。
     * 【技巧】ConcurrentHashMap 覆寫。
     * 【概念】RetryAspect 成功或耗盡時寫入，測試可斷言 attempts ≥ 2。
     */
    public void recordRetryAttempts(String method, int attempts) {
        retryAttempts.put(method, attempts);
    }

    public List<String> getLogs() {
        return logs;
    }

    public Map<String, Long> getTimings() {
        return timings;
    }

    public List<String> getAudits() {
        return audits;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public int getCacheHits() {
        return cacheHits.get();
    }

    public int getCacheMisses() {
        return cacheMisses.get();
    }

    public Map<String, Integer> getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * 【職責】清空所有記錄（測試前重置）。
     * 【技巧】各集合 clear／計數歸零。
     * 【概念】整合測試隔離：避免前一 CASE 的觀測污染下一 CASE。
     */
    public void clear() {
        logs.clear();
        timings.clear();
        audits.clear();
        exceptions.clear();
        cacheHits.set(0);
        cacheMisses.set(0);
        retryAttempts.clear();
    }
}
