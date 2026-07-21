package com.trading.app.aspect;

import com.trading.common.OrderResult;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 【職責】切面 6／6：下單成功回傳後寫入稽核軌跡。
 * 【技巧】{@code @AfterReturning(pointcut=…, returning="result")}；只綁 {@code placeOrder}；{@code @Order(60)}。
 * 【概念】稽核是合規橫切關注點——「成交後留痕」不應塞進業務方法尾端。{@code @AfterReturning} 只在正常返回觸發（例外路徑不寫成功稽核）；與 {@code @After}/{@code @AfterThrowing} 互補。
 * 【邊界】不修改回傳值；風控拒絕等例外路徑不會進此方法。
 */
@Aspect
@Component
@Order(60)
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AspectRecorder recorder;

    /**
     * 【職責】注入共用觀測器。
     * 【技巧】建構子注入。
     * 【概念】稽核列可被報表 API 與整合測試讀取。
     */
    public AuditAspect(AspectRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * 【職責】將成交摘要寫入稽核記錄。
     * 【技巧】{@code returning} 綁定回傳的 {@link OrderResult}；無需 JoinPoint.proceed。
     * 【概念】AfterReturning＝「成功後通知」。比在 Service 末尾手寫 audit 更不易漏、也更易統一格式。
     * @param result 下單成功回傳值
     */
    @AfterReturning(
            pointcut = "execution(* com.trading.app.application.OrderService.placeOrder(..))",
            returning = "result")
    public void audit(OrderResult result) {
        String entry = "AUDIT orderId=" + result.getOrderId()
                + " client=" + result.getClientOrderId()
                + " symbol=" + result.getSymbol()
                + " status=" + result.getStatus();
        recorder.recordAudit(entry);
        log.info("[AUDIT] {}", entry);
    }
}
