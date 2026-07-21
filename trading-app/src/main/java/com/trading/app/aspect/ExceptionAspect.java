package com.trading.app.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 【職責】切面 3／6：Service 拋例外時集中記錄告警（不吞例外）。
 * 【技巧】{@code @AfterThrowing(pointcut=…, throwing="ex")}；{@code @Order(30)}；只有例外路徑觸發。
 * 【概念】「例外觀測」是橫切關注點。{@code @AfterThrowing} 適合只關心失敗路徑；與 {@code @Around} 的 catch 不同，它不能改回傳，也預設不阻止例外繼續傳播——與 GlobalExceptionHandler（HTTP 映射）分工。
 * 【邊界】不負責轉 HTTP 狀態；不修改例外。
 */
@Aspect
@Component
@Order(30)
public class ExceptionAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionAspect.class);

    private final AspectRecorder recorder;

    /**
     * 【職責】注入共用觀測器。
     * 【技巧】建構子注入。
     * 【概念】切面可被測試注入同一 Recorder 斷言「有記到 RiskRejectedException」。
     */
    public ExceptionAspect(AspectRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * 【職責】攔截 Service 拋出的例外並寫入觀測紀錄。
     * 【技巧】{@code @AfterThrowing}；{@link JoinPoint} 取簽名（無需 proceed）。
     * 【概念】AfterThrowing＝「失敗後通知」；對照 Around 的 catch：這裡刻意不包 proceed，語意更窄、更安全（不會誤吞）。
     */
    @AfterThrowing(pointcut = "execution(* com.trading.app.application.*Service.*(..))", throwing = "ex")
    public void onException(JoinPoint joinPoint, Throwable ex) {
        String signature = joinPoint.getSignature().toShortString();
        recorder.recordException(signature + " → " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        log.warn("[EXCEPTION] {} 拋出 {}", signature, ex.getClass().getSimpleName());
    }
}
