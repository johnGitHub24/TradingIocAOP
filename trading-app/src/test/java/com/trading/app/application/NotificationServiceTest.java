package com.trading.app.application;

import com.trading.common.OrderResult;
import com.trading.common.OrderStatus;
import com.trading.common.Side;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 【職責】驗證 NotificationService 失敗／成功門檻（不含 RetryAspect）。
 * 【技巧】直接 {@code new}；{@code setFailuresBeforeSuccess} 控制第幾次才過。
 * 【概念】單元測「本體第 N 次會丟」；重試迴圈留給 RETRY_INT_001。
 */
@Tag("unit")
class NotificationServiceTest {

    private static OrderResult filled(String clientOrderId) {
        return new OrderResult("ORD-1", clientOrderId, "AAPL", Side.BUY, 10,
                BigDecimal.valueOf(100), OrderStatus.FILLED, "成交");
    }

    /**
     * CASE NOTIFY_002：預設失敗一次後第二次成功。
     * Given: failuresBeforeSuccess=1；When: 同 client 呼叫兩次；Then: 第一次拋 NotificationException，第二次不拋。
     * 【技巧驗證】attempt 計次與門檻。
     */
    @Test
    void NOTIFY_002_failsOnceThenSucceeds() {
        NotificationService service = new NotificationService();
        service.setFailuresBeforeSuccess(1);

        assertThatThrownBy(() -> service.notifyFilled(filled("C-notify")))
                .isInstanceOf(NotificationException.class);
        assertThatCode(() -> service.notifyFilled(filled("C-notify")))
                .doesNotThrowAnyException();
    }
}
