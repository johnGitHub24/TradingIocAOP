package com.trading.miniioc.demo;

import com.trading.common.OrderRequest;
import com.trading.common.OrderResult;
import com.trading.common.OrderStatus;
import com.trading.common.Side;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】驗證 SimpleOrderPlacer 編排：風控過則 FILLED、不過則 REJECTED。
 * 【技巧】直接 {@code new} 真實協作者，不經容器／代理。
 * 【概念】單元測業務編排；IoC 解析留給 IOC_001，代理留給 AOP／IOC_INT_001。
 */
@Tag("unit")
class SimpleOrderPlacerTest {

    private final SimpleOrderPlacer placer =
            new SimpleOrderPlacer(new SimpleRiskChecker(), new SimplePricingGateway());

    /**
     * CASE MINI_ORDER_001：合法數量成交並帶中間價。
     * Given: qty=10；When: place；Then: FILLED 且 executedPrice 非空。
     */
    @Test
    void MINI_ORDER_001_approved_returnsFilled() {
        OrderResult result = placer.place(new OrderRequest("C-1", "AAPL", Side.BUY, 10, BigDecimal.TEN));

        assertThat(result.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(result.getExecutedPrice()).isNotNull();
        assertThat(result.getOrderId()).startsWith("MINI-");
    }

    /**
     * CASE MINI_ORDER_002：超量風控拒絕、無成交價。
     * Given: qty=9999；When: place；Then: REJECTED、executedPrice 為 null。
     */
    @Test
    void MINI_ORDER_002_rejected_returnsRejectedWithoutPrice() {
        OrderResult result = placer.place(new OrderRequest("C-2", "AAPL", Side.BUY, 9999, BigDecimal.TEN));

        assertThat(result.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(result.getExecutedPrice()).isNull();
        assertThat(result.getMessage()).contains("風控");
    }
}
