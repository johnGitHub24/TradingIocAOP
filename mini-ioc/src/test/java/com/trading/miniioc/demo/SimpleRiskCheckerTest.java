package com.trading.miniioc.demo;

import com.trading.common.OrderRequest;
import com.trading.common.Side;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】驗證 SimpleRiskChecker 數量上下限。
 * 【技巧】純 POJO {@code new}，回傳 boolean（對照 trading-app 拋例外）。
 * 【概念】手刻軌用「過不過」表達拒絕，呼叫端決定組 REJECTED。
 */
@Tag("unit")
class SimpleRiskCheckerTest {

    private final SimpleRiskChecker checker = new SimpleRiskChecker();

    /**
     * CASE MINI_RISK_001：數量在 1～1000 內通過；超量拒絕。
     * Given: qty=10 與 qty=1001；When: approve；Then: true／false。
     */
    @Test
    void MINI_RISK_001_quantityBounds() {
        assertThat(checker.approve(new OrderRequest("C-1", "AAPL", Side.BUY, 10, BigDecimal.TEN))).isTrue();
        assertThat(checker.approve(new OrderRequest("C-2", "AAPL", Side.BUY, 1001, BigDecimal.TEN))).isFalse();
        assertThat(checker.approve(new OrderRequest("C-3", "AAPL", Side.BUY, 0, BigDecimal.TEN))).isFalse();
    }
}
