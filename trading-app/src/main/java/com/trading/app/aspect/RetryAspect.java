package com.trading.app.aspect;

import com.trading.app.aspect.annotation.Retryable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 【職責】切面 4／6：標註 {@link Retryable} 的方法失敗時自動重試。
 * 【技巧】{@code @Around("@annotation(retryable)")} 綁定註解參數；迴圈呼叫 {@code proceed()}；{@code @Order(40)}。
 * 【概念】重試是橫切關注點——業務方法只寫「送通知」，不寫 for-retry。註解驅動切點＝「誰需要重試誰貼標」，比 execution 全包更精準。
 * 【邊界】耗盡後拋最後一次例外；不區分例外類型（教學簡化）。
 */
@Aspect
@Component
@Order(40)
public class RetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RetryAspect.class);

    private final AspectRecorder recorder;

    /**
     * 【職責】注入共用觀測器。
     * 【技巧】建構子注入。
     * 【概念】記錄 attempts 供整合測試驗證「真的重試過」。
     */
    public RetryAspect(AspectRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * 【職責】依 {@link Retryable#maxAttempts()} 重試，成功或耗盡時寫入 Recorder。
     * 【技巧】註解綁定參數 {@code Retryable retryable}；每次失敗再 proceed。
     * 【概念】Around 才能「決定要不要再呼叫目標」；Before／After 做不到重試迴圈。
     */
    @Around("@annotation(retryable)")
    public Object retry(ProceedingJoinPoint joinPoint, Retryable retryable) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        int maxAttempts = retryable.maxAttempts();
        Throwable lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Object result = joinPoint.proceed();
                recorder.recordRetryAttempts(signature, attempt);
                return result;
            } catch (Throwable t) {
                lastError = t;
                log.warn("[RETRY] {} 第 {}/{} 次失敗：{}", signature, attempt, maxAttempts, t.getMessage());
            }
        }
        recorder.recordRetryAttempts(signature, maxAttempts);
        throw lastError;
    }
}
