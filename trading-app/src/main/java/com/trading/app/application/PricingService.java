package com.trading.app.application;

import com.trading.app.aspect.annotation.Cacheable;
import com.trading.common.Quote;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 【職責】計算標的報價；相同 symbol 可被 CacheAspect 快取。
 * 【技巧】方法標 {@link Cacheable}；{@link #getComputeCount()} 證明本體執行次數。
 * 【概念】業務方法「假裝每次都算」；真正是否進本體由切面決定。這是宣告式 AOP 的體感：貼標＝加入快取橫切。
 * 【邊界】假行情（hash 推導），非真實市價。
 */
@Service
public class PricingService {

    private final AtomicInteger computeCount = new AtomicInteger();

    /**
     * 【職責】計算並回傳報價。
     * 【技巧】{@code @Cacheable("quotes")} 觸發 CacheAspect；computeCount 僅在本體執行時遞增。
     * 【概念】第二次同參數呼叫若 count 仍為 1，即證明 Around 短路成功。
     * @param symbol 標的代碼
     * @return bid／ask 報價
     */
    @Cacheable("quotes")
    public Quote getQuote(String symbol) {
        computeCount.incrementAndGet();
        int base = Math.abs(symbol.hashCode() % 1000) + 100;
        BigDecimal bid = BigDecimal.valueOf(base).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ask = bid.add(BigDecimal.valueOf(0.5));
        return new Quote(symbol, bid, ask);
    }

    /** 方法本體實際執行次數（證明快取是否命中）。 */
    public int getComputeCount() {
        return computeCount.get();
    }

    /**
     * 【職責】測試用：重置計算計數。
     * 【技巧】AtomicInteger set 0。
     * 【概念】與 CacheAspect.clear 搭配做 CASE 隔離。
     */
    public void resetComputeCount() {
        computeCount.set(0);
    }
}
