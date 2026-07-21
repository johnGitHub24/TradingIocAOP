package com.trading.miniioc.container;

import com.trading.common.OrderRequest;
import com.trading.common.OrderResult;
import com.trading.common.OrderStatus;
import com.trading.common.Side;
import com.trading.miniioc.demo.OrderPlacer;
import com.trading.miniioc.demo.PricingGateway;
import com.trading.miniioc.demo.SimpleOrderPlacer;
import com.trading.miniioc.demo.SimplePricingGateway;
import com.trading.miniioc.demo.SimpleRiskChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 【職責】驗證手刻 IoC：建構子注入、單例、掃描與找不到 bean 的錯誤。
 * 【技巧】手動 register／scan；AssertJ 斷言同一實例與例外型別。
 * 【概念】保護控制反轉核心行為——容器決定相依從哪來，呼叫端只認介面。
 */
@Tag("unit")
class MiniApplicationContextTest {

    /**
     * CASE IOC_001：建構子注入解析相依圖並可下單。
     * Given: 註冊 Placer／Risk／Pricing；When: getBean(OrderPlacer) 後 place；Then: FILLED 且有成交價。
     * 【技巧驗證】遞迴 getBean 注入建構子參數。
     */
    @Test
    void IOC_001_constructorInjection_resolvesDependencyGraph() {
        MiniApplicationContext context = new MiniApplicationContext()
                .register(SimpleOrderPlacer.class, SimpleRiskChecker.class, SimplePricingGateway.class);

        OrderPlacer placer = context.getBean(OrderPlacer.class);

        OrderResult result = placer.place(new OrderRequest("C-1", "AAPL", Side.BUY, 10, BigDecimal.TEN));
        assertThat(placer).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(result.getExecutedPrice()).isNotNull();
    }

    /**
     * CASE IOC_002：同型別兩次 getBean 回同一單例。
     * Given: 註冊 PricingGateway；When: getBean 兩次；Then: isSameAs。
     * 【技巧驗證】singletons 快取。
     */
    @Test
    void IOC_002_singleton_returnsSameInstance() {
        MiniApplicationContext context = new MiniApplicationContext()
                .register(SimplePricingGateway.class);

        PricingGateway first = context.getBean(PricingGateway.class);
        PricingGateway second = context.getBean(PricingGateway.class);

        assertThat(first).isSameAs(second);
    }

    /**
     * CASE IOC_003：未註冊型別拋 BeanException。
     * Given: 空容器；When: getBean(OrderPlacer)；Then: BeanException 訊息含「找不到型別」。
     * 【技巧驗證】失敗快速、錯誤可診斷。
     */
    @Test
    void IOC_003_unknownType_throwsBeanException() {
        MiniApplicationContext context = new MiniApplicationContext();

        assertThatThrownBy(() -> context.getBean(OrderPlacer.class))
                .isInstanceOf(BeanException.class)
                .hasMessageContaining("找不到型別");
    }

    /**
     * CASE IOC_004：scan 發現 @Component。
     * Given: scan demo 套件；When: 查 componentCount 與 getBean；Then: ≥3 且 OrderPlacer 非 null。
     * 【技巧驗證】ClasspathScanner + @Component。
     */
    @Test
    void IOC_004_scan_discoversAnnotatedComponents() {
        MiniApplicationContext context = new MiniApplicationContext()
                .scan("com.trading.miniioc.demo");

        assertThat(context.getComponentCount()).isGreaterThanOrEqualTo(3);
        assertThat(context.getBean(OrderPlacer.class)).isNotNull();
    }
}
