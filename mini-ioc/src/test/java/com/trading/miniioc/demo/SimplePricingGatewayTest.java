package com.trading.miniioc.demo;

import com.trading.common.Quote;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】驗證 SimplePricingGateway 假報價可重現。
 * 【技巧】hash 推導 bid／ask；同 symbol 兩次結果相同。
 * 【概念】介面可替換：單元測穩定假資料，不必連外部行情。
 */
@Tag("unit")
class SimplePricingGatewayTest {

    /**
     * CASE MINI_PRICE_001：同 symbol 報價穩定且 ask = bid + 0.5。
     * Given: "AAPL"；When: quote 兩次；Then: bid／ask／mid 齊備且兩次相等。
     */
    @Test
    void MINI_PRICE_001_quote_isStableAndSpreadIsHalf() {
        SimplePricingGateway gateway = new SimplePricingGateway();

        Quote first = gateway.quote("AAPL");
        Quote second = gateway.quote("AAPL");

        assertThat(first.getBid()).isNotNull();
        assertThat(first.getAsk()).isEqualByComparingTo(first.getBid().add(BigDecimal.valueOf(0.5)));
        assertThat(first.mid()).isNotNull();
        assertThat(second.getBid()).isEqualByComparingTo(first.getBid());
    }
}
