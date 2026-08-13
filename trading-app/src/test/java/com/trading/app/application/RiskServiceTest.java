package com.trading.app.application;

import com.trading.common.OrderRequest;
import com.trading.common.Side;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 【職責】驗證 RiskService 數量／名目金額規則與 ruleCode。
 * 【技巧】純 POJO new，不啟動 Spring。
 * 【概念】風控規則應可離線單測；例外碼供 API 契約穩定。
 */
@Tag("unit")
class RiskServiceTest {

    private final RiskService riskService = new RiskService();

    private OrderRequest request(int quantity, BigDecimal price) {
        return new OrderRequest("C-1", "AAPL", Side.BUY, quantity, price);
    }

    /**
     * CASE RISK_001：限額內通過。
     * Given: qty=10 price=100；When: check；Then: 不拋例外。
     */
    @Test
    void RISK_001_withinLimits_passes() {
        assertThatCode(() -> riskService.check(request(10, BigDecimal.valueOf(100))))
                .doesNotThrowAnyException();
    }

    /**
     * CASE RISK_002 / ORDER-002：數量超限拋 R002。
     * Given: qty=5000（對齊 fixture ORDER-002-RISK_QTY）；When: check；Then: RiskRejectedException.ruleCode=R002。
     * 【技巧驗證】與整合 ORDER-002 同一風控契約。
     */
    @Test
    void RISK_002_quantityOverLimit_throwsR002() {
        assertThatThrownBy(() -> riskService.check(request(5000, BigDecimal.valueOf(100))))
                .isInstanceOfSatisfying(RiskRejectedException.class,
                        e -> org.assertj.core.api.Assertions.assertThat(e.getRuleCode()).isEqualTo("R002"));
    }

    /**
     * CASE RISK_003：名目金額超限拋 R003。
     * Given: qty=1000 price=2000（名目 2M）；When: check；Then: ruleCode=R003。
     */
    @Test
    void RISK_003_notionalOverLimit_throwsR003() {
        // 數量在上限內（1000），但名目金額 1000 * 2000 = 2,000,000 超過上限。
        assertThatThrownBy(() -> riskService.check(request(1000, BigDecimal.valueOf(2000))))
                .isInstanceOfSatisfying(RiskRejectedException.class,
                        e -> org.assertj.core.api.Assertions.assertThat(e.getRuleCode()).isEqualTo("R003"));
    }
}
