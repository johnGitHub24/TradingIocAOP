package com.trading.miniioc.integration;

import com.trading.common.OrderRequest;
import com.trading.common.OrderResult;
import com.trading.common.OrderStatus;
import com.trading.common.Side;
import com.trading.miniioc.aop.LoggingInterceptor;
import com.trading.miniioc.aop.TimingInterceptor;
import com.trading.miniioc.container.MiniApplicationContext;
import com.trading.miniioc.demo.OrderPlacer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】手刻容器整合：掃描 + DI + AOP 代理 + 下單端到端。
 * 【技巧】scan + 雙攔截器；以介面取 bean 後下兩筆單。
 * 【概念】保護「IoC 與 AOP 接點」整條鏈仍可用——對照 trading-app 的 SpringBootTest。
 */
@Tag("integration")
class MiniIocIntegrationTest {

    /**
     * CASE IOC_INT_001：全流程成交與風控拒絕，且攔截器有紀錄。
     * Given: scan demo + Logging + Timing；When: 合法單與超量單；Then: FILLED／REJECTED，logs 非空，timings 含 place。
     * 【技巧驗證】代理後業務結果與橫切觀測同時成立。
     */
    @Test
    void IOC_INT_001_fullFlow_scanInjectProxyAndPlaceOrder() {
        LoggingInterceptor logging = new LoggingInterceptor();
        TimingInterceptor timing = new TimingInterceptor();

        MiniApplicationContext context = new MiniApplicationContext()
                .scan("com.trading.miniioc.demo")
                .addInterceptor(logging)
                .addInterceptor(timing);

        OrderPlacer placer = context.getBean(OrderPlacer.class);

        OrderResult filled = placer.place(new OrderRequest("C-1", "AAPL", Side.BUY, 10, BigDecimal.TEN));
        OrderResult rejected = placer.place(new OrderRequest("C-2", "AAPL", Side.BUY, 9999, BigDecimal.TEN));

        assertThat(filled.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(rejected.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(logging.getLogs()).isNotEmpty();
        assertThat(timing.getTimings()).containsKey("place");
    }
}
