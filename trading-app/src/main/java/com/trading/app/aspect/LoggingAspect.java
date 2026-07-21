package com.trading.app.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 【職責】切面 1／6：記錄 application 層 Service 方法的進出與參數。
 * 【技巧】{@code @Aspect} + {@code @Around} + execution 切點；{@code @Order(10)} 較早包覆；寫入 {@link AspectRecorder}。
 * 【概念】日誌是橫切關注點——不應複製貼上到每個 Service 方法。{@code @Around} 可同時處理進入、離開、例外；{@code @Order} 數字越小越外層（先 before、後 after）。
 * 【邊界】只觀測，不改變回傳值；例外記錄後仍向上拋。
 */
@Aspect
@Component
@Order(10)
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    private final AspectRecorder recorder;

    /**
     * 【職責】注入共用觀測器。
     * 【技巧】建構子注入（Spring IoC）。
     * 【概念】切面本身也是 bean，可依賴其他 bean——AOP 與 DI 是同一容器裡的兩件事。
     */
    public LoggingAspect(AspectRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * 【職責】在 Service 方法前後寫進出日誌。
     * 【技巧】{@code @Around("execution(* …*Service.*(..))")}；{@link ProceedingJoinPoint#proceed()}。
     * 【概念】切點表達式選定「織入點」；業務方法無感。對照 mini-ioc {@code LoggingInterceptor}——同一橫切，掛載從手刻變成宣告式。
     * @param joinPoint 進行中的連接點
     * @return 目標方法回傳值
     */
    @Around("execution(* com.trading.app.application.*Service.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        recorder.recordLog("→ 進入 " + signature + " 參數=" + Arrays.toString(joinPoint.getArgs()));
        log.info("→ 進入 {}", signature);
        try {
            Object result = joinPoint.proceed();
            recorder.recordLog("← 離開 " + signature);
            log.info("← 離開 {}", signature);
            return result;
        } catch (Throwable t) {
            recorder.recordLog("✗ " + signature + " 拋出 " + t.getClass().getSimpleName());
            throw t;
        }
    }
}
