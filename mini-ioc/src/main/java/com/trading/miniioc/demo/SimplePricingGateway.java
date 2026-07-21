package com.trading.miniioc.demo;

import com.trading.common.Quote;
import com.trading.miniioc.annotation.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 【職責】以 symbol 推導穩定假報價的示範實作（不連外部行情）。
 * 【技巧】{@code @Component} 供掃描；hash 推導可重現的 bid／ask。
 * 【概念】示範「介面 + 可替換實作」：換真行情只需新實作同一介面，OrderPlacer 不用改。
 */
@Component
public class SimplePricingGateway implements PricingGateway {

    /**
     * 【職責】依 symbol 產生可重現的 bid／ask。
     * 【技巧】{@code hashCode} 取模當基準價；{@link BigDecimal} 定 scale。
     * 【概念】教學用假資料讓結果穩定，方便斷言與 demo 輸出。
     */
    @Override
    public Quote quote(String symbol) {
        // 由 symbol 產生一個可重現的基準價，讓示範結果穩定。
        int base = Math.abs(symbol.hashCode() % 1000) + 100;
        BigDecimal bid = BigDecimal.valueOf(base).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ask = bid.add(BigDecimal.valueOf(0.5));
        return new Quote(symbol, bid, ask);
    }
}
