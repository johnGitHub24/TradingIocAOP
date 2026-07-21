package com.trading.app.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 【職責】切面 2／6：量測 Service 方法耗時，超過門檻則告警。
 * 【技巧】{@code @Around} + {@code finally} 計時；{@code @Order(20)}；慢呼叫打 warn。
 * 【概念】效能監控是橫切關注點：用 Around 才能包住整段執行（含例外路徑）。與 {@code @Before}/{@code @After} 拆開計時相比，Around 不易漏算。
 * 【邊界】只觀測與告警，不中斷慢呼叫。
 */
@Aspect
@Component
@Order(20)
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);
    private static final long SLOW_THRESHOLD_MS = 200;

    private final AspectRecorder recorder;

    /**
     * 【職責】注入共用觀測器。
     * 【技巧】建構子注入。
     * 【概念】與 LoggingAspect 共用 Recorder，報表一次看齊。
     */
    public PerformanceAspect(AspectRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * 【職責】量測耗時並寫入 Recorder；逾門檻記 warn。
     * 【技巧】nanoTime；{@code finally} 保證例外也記錄。
     * 【概念】對照 mini-ioc {@code TimingInterceptor}：概念相同，這裡用 Spring 切點自動套到所有 *Service。
     */
    @Around("execution(* com.trading.app.application.*Service.*(..))")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            recorder.recordTiming(signature, elapsedMs);
            if (elapsedMs > SLOW_THRESHOLD_MS) {
                log.warn("[SLOW] {} 耗時 {} ms 超過門檻 {} ms", signature, elapsedMs, SLOW_THRESHOLD_MS);
            }
        }
    }
}
