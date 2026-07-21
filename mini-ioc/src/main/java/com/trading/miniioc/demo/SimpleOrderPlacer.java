package com.trading.miniioc.demo;

import com.trading.common.OrderRequest;
import com.trading.common.OrderResult;
import com.trading.common.OrderStatus;
import com.trading.miniioc.annotation.Component;

import java.util.UUID;

/**
 * 【職責】下單編排：風控 → 取報價 → 組裝結果。
 * 【技巧】建構子注入 {@link RiskChecker}／{@link PricingGateway}；標 {@code @Component}。
 * 【概念】「控制反轉」體感：本類不 {@code new} 相依，由容器傳入。對照 trading-app {@code OrderService}——同一編排思路，容器從手刻換成 Spring。
 * 【邊界】不落庫、不發通知；僅示範 DI／AOP 掛載點。
 */
@Component
public class SimpleOrderPlacer implements OrderPlacer {

    private final RiskChecker riskChecker;
    private final PricingGateway pricingGateway;

    /**
     * 【職責】接收容器提供的風控與報價相依。
     * 【技巧】單一建構子＝容器自動選用，無需 {@code @Inject}。
     * 【概念】相依以介面型別宣告，實作可替換且可被代理。
     */
    public SimpleOrderPlacer(RiskChecker riskChecker, PricingGateway pricingGateway) {
        this.riskChecker = riskChecker;
        this.pricingGateway = pricingGateway;
    }

    /**
     * 【職責】風控未過回 REJECTED；通過則以報價中間價回 FILLED。
     * 【技巧】編排順序固定；orderId 用 UUID 前綴。
     * 【概念】業務方法本身無日誌／計時——這些由攔截器橫切掛上，保持方法專注編排。
     */
    @Override
    public OrderResult place(OrderRequest request) {
        String orderId = "MINI-" + UUID.randomUUID().toString().substring(0, 8);
        if (!riskChecker.approve(request)) {
            return new OrderResult(orderId, request.getClientOrderId(), request.getSymbol(),
                    request.getSide(), request.getQuantity(), null,
                    OrderStatus.REJECTED, "風控未通過");
        }
        var quote = pricingGateway.quote(request.getSymbol());
        return new OrderResult(orderId, request.getClientOrderId(), request.getSymbol(),
                request.getSide(), request.getQuantity(), quote.mid(),
                OrderStatus.FILLED, "成交");
    }
}
