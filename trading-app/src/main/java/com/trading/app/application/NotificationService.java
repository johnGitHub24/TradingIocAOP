package com.trading.app.application;

import com.trading.app.aspect.annotation.Retryable;
import com.trading.common.OrderResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 【職責】模擬不穩定的成交通知通道，供 RetryAspect 示範。
 * 【技巧】{@code @Retryable}；以 clientOrderId 計次，前 N 次拋 {@link NotificationException}。
 * 【概念】業務方法只寫「送通知」；重試迴圈在切面。可調 {@link #setFailuresBeforeSuccess} 控制何時成功，方便測試。
 * 【邊界】不實際發 email／MQ。
 */
@Service
public class NotificationService {

    private final Map<String, Integer> attemptsPerClient = new ConcurrentHashMap<>();
    private int failuresBeforeSuccess = 1;

    /**
     * 【職責】模擬成交通知；前 N 次失敗以觸發重試。
     * 【技巧】{@code @Retryable(maxAttempts = 3)}；merge 計次。
     * 【概念】方法本體「每次被呼叫」都算一次 attempt；切面決定要呼叫幾次。
     */
    @Retryable(maxAttempts = 3)
    public void notifyFilled(OrderResult result) {
        int attempt = attemptsPerClient.merge(result.getClientOrderId(), 1, Integer::sum);
        if (attempt <= failuresBeforeSuccess) {
            throw new NotificationException("模擬通知通道暫時失敗（第 " + attempt + " 次）");
        }
        // 成功送出（此處僅示範，不實際發送）。
    }

    /**
     * 【職責】調整「成功前要失敗幾次」，供測試驗證重試。
     * 【技巧】可變狀態欄位（僅測試／示範）。
     * 【概念】設為 ≥ maxAttempts 可觀察「重試耗盡仍失敗」路徑。
     */
    public void setFailuresBeforeSuccess(int failuresBeforeSuccess) {
        this.failuresBeforeSuccess = failuresBeforeSuccess;
    }

    /**
     * 【職責】測試用：清空各 client 的嘗試計數。
     * 【技巧】Map.clear。
     * 【概念】CASE 隔離，避免計次跨測試累積。
     */
    public void reset() {
        attemptsPerClient.clear();
    }
}
