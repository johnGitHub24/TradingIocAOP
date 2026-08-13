package com.trading.app.application;

import com.trading.common.Quote;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】驗證 PricingService 假報價計算與計數器（不含 CacheAspect）。
 * 【技巧】直接 {@code new}，不啟動 Spring／AOP。
 * 【概念】單元測「本體每次都會算」；快取命中留給 CACHE_INT_001。
 */
@Tag("unit")
class PricingServiceTest {

    /**
     * CASE PRICE_001：同 symbol 兩次呼叫皆遞增 computeCount，且 bid／ask 穩定。
     * Given: 新 PricingService；When: getQuote("AAPL") 兩次；Then: count=2、mid 非空、兩次報價相同。
     * 【技巧驗證】hash 推導可重現；切面未織入時不會短路。
     */
    @Test
    void PRICE_001_getQuote_incrementsComputeCountAndIsStable() {
        PricingService service = new PricingService();

        Quote first = service.getQuote("AAPL");
        Quote second = service.getQuote("AAPL");

        assertThat(service.getComputeCount()).isEqualTo(2);
        assertThat(first.getBid()).isNotNull();
        assertThat(first.getAsk()).isEqualByComparingTo(first.getBid().add(BigDecimal.valueOf(0.5)));
        assertThat(first.mid()).isNotNull();
        assertThat(second.getBid()).isEqualByComparingTo(first.getBid());
    }
}
