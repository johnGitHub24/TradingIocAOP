package com.trading.miniioc.aop;

import com.trading.common.OrderRequest;
import com.trading.common.Side;
import com.trading.miniioc.container.MiniApplicationContext;
import com.trading.miniioc.demo.OrderPlacer;
import com.trading.miniioc.demo.PricingGateway;
import com.trading.miniioc.demo.SimpleOrderPlacer;
import com.trading.miniioc.demo.SimplePricingGateway;
import com.trading.miniioc.demo.SimpleRiskChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】驗證手刻 AOP：JDK 動態代理 + 攔截器鏈的進出日誌、計時與執行順序。
 * 【技巧】MiniApplicationContext 掛攔截器後以介面取 bean；ProxyFactory 直接測鏈順序。
 * 【概念】保護「橫切真的包到業務方法」與「先登記者為外層」——對照 Spring {@code @Order}。
 */
@Tag("unit")
class ProxyInterceptorTest {

    private MiniApplicationContext contextWith(MethodInterceptor... interceptors) {
        MiniApplicationContext context = new MiniApplicationContext()
                .register(SimpleOrderPlacer.class, SimpleRiskChecker.class, SimplePricingGateway.class);
        for (MethodInterceptor interceptor : interceptors) {
            context.addInterceptor(interceptor);
        }
        return context;
    }

    /**
     * CASE AOP_001：LoggingInterceptor 記錄進出。
     * Given: 容器掛 LoggingInterceptor；When: place 合法單；Then: logs 含進入／離開 place。
     * 【技巧驗證】Around 風格攔截器在 proceed 前後寫入。
     */
    @Test
    void AOP_001_loggingInterceptor_recordsEnterAndExit() {
        LoggingInterceptor logging = new LoggingInterceptor();
        OrderPlacer placer = contextWith(logging).getBean(OrderPlacer.class);

        placer.place(new OrderRequest("C-1", "AAPL", Side.BUY, 10, BigDecimal.TEN));

        assertThat(logging.getLogs())
                .anyMatch(line -> line.contains("進入 place"))
                .anyMatch(line -> line.contains("離開 place"));
    }

    /**
     * CASE AOP_002：TimingInterceptor 記錄耗時。
     * Given: 掛 TimingInterceptor；When: place；Then: timings 含 place 且 ≥ 0。
     * 【技巧驗證】finally 計時（成功路徑也寫入）。
     */
    @Test
    void AOP_002_timingInterceptor_recordsElapsed() {
        TimingInterceptor timing = new TimingInterceptor();
        OrderPlacer placer = contextWith(timing).getBean(OrderPlacer.class);

        placer.place(new OrderRequest("C-1", "AAPL", Side.BUY, 10, BigDecimal.TEN));

        assertThat(timing.getTimings()).containsKey("place");
        assertThat(timing.getTimings().get("place")).isGreaterThanOrEqualTo(0L);
    }

    /**
     * CASE AOP_003：攔截器鏈依登記順序洋蔥式執行。
     * Given: outer→inner 兩攔截器；When: quote；Then: outer-before, inner-before, inner-after, outer-after。
     * 【技巧驗證】InterceptorChain 索引推進順序。
     */
    @Test
    void AOP_003_interceptorChain_executesInRegisteredOrder() {
        // 直接對「無巢狀呼叫」的元件建立代理，單純驗證攔截器鏈的洋蔥式包覆順序。
        List<String> order = new ArrayList<>();
        MethodInterceptor outer = invocation -> {
            order.add("outer-before");
            Object r = invocation.proceed();
            order.add("outer-after");
            return r;
        };
        MethodInterceptor inner = invocation -> {
            order.add("inner-before");
            Object r = invocation.proceed();
            order.add("inner-after");
            return r;
        };

        PricingGateway proxy = (PricingGateway) ProxyFactory.createProxy(
                new SimplePricingGateway(), List.of(outer, inner));
        proxy.quote("AAPL");

        assertThat(order).containsExactly("outer-before", "inner-before", "inner-after", "outer-after");
    }
}
