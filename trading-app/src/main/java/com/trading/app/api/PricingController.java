package com.trading.app.api;

import com.trading.app.application.PricingService;
import com.trading.common.Quote;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【職責】報價 REST 入口；連續查同一 symbol 可觀察 CacheAspect。
 * 【技巧】{@code @RestController}；路徑變數 symbol。
 * 【概念】專門暴露「快取命中」教學場景，不必每次走完整下單流程。
 */
@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PricingService pricingService;

    /**
     * 【職責】注入報價服務。
     * 【技巧】建構子注入。
     * 【概念】與 OrderController 相同的薄層模式。
     */
    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * 【職責】依標的查詢報價（可能命中快取）。
     * 【技巧】委派 {@link PricingService#getQuote}。
     * 【概念】第二次相同 symbol 應走 CacheAspect 短路，computeCount 不增加。
     */
    @GetMapping("/{symbol}")
    public Quote quote(@PathVariable String symbol) {
        return pricingService.getQuote(symbol);
    }
}
