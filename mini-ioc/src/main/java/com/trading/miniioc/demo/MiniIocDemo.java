package com.trading.miniioc.demo;

import com.trading.common.OrderRequest;
import com.trading.common.OrderResult;
import com.trading.common.Side;
import com.trading.miniioc.aop.LoggingInterceptor;
import com.trading.miniioc.aop.TimingInterceptor;
import com.trading.miniioc.container.MiniApplicationContext;

import java.math.BigDecimal;

/**
 * 【職責】手刻容器端到端示範：掃描 → 掛攔截器 → 取代理 bean → 下單。
 * 【技巧】{@link MiniApplicationContext#scan}／{@code addInterceptor}／{@code getBean(介面)}；執行 {@code ./gradlew :mini-ioc:run}。
 * 【概念】把 IoC（誰建立誰）與 AOP（誰包誰）串成一條可見流程；對照之後啟動 trading-app 看 Spring 做同一件事的工業級版本。
 */
public class MiniIocDemo {

    /**
     * 【職責】跑兩種情境（成交／風控拒絕）並印出結果與攔截器輸出。
     * 【技巧】以介面型別取 bean，確保拿到 JDK 代理。
     * 【概念】若改用實作類 {@code getBean(SimpleOrderPlacer.class)}，在有代理時可能型別不符——凸顯「面向介面」的必要性。
     */
    public static void main(String[] args) {
        MiniApplicationContext context = new MiniApplicationContext()
                .scan("com.trading.miniioc.demo")
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new TimingInterceptor());

        // 以介面型別取得 bean，拿到的是被攔截器包裝過的代理。
        OrderPlacer placer = context.getBean(OrderPlacer.class);

        System.out.println("===== 情境 1：正常下單 =====");
        OrderResult ok = placer.place(new OrderRequest("C-1", "AAPL", Side.BUY, 10, BigDecimal.TEN));
        System.out.println("結果：" + ok.getStatus() + " @ " + ok.getExecutedPrice());

        System.out.println("\n===== 情境 2：數量超限被風控拒絕 =====");
        OrderResult rejected = placer.place(new OrderRequest("C-2", "AAPL", Side.BUY, 5000, BigDecimal.TEN));
        System.out.println("結果：" + rejected.getStatus() + "（" + rejected.getMessage() + "）");
    }
}
