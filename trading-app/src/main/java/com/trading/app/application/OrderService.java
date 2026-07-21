package com.trading.app.application;

import com.trading.app.infrastructure.OrderEntity;
import com.trading.app.infrastructure.OrderRepository;
import com.trading.common.OrderRequest;
import com.trading.common.OrderResult;
import com.trading.common.OrderStatus;
import com.trading.common.Quote;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 【職責】下單編排：風控 → 報價 → 落庫 → 通知。
 * 【技巧】{@code @Service} + 建構子注入四個協作者；{@code @Transactional} 包寫入。
 * 【概念】本類是 DI 主示範點——完全不 {@code new} 相依。日誌／計時／稽核／重試／快取由六大切面橫切掛上，方法只留業務編排。對照 mini-ioc {@code SimpleOrderPlacer}。
 * 【邊界】不處理 HTTP；不實作切面邏輯。
 */
@Service
public class OrderService {

    private final RiskService riskService;
    private final PricingService pricingService;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;

    /**
     * 【職責】接收 Spring 注入的風控、報價、通知與 Repository。
     * 【技巧】建構子注入（推薦於 field {@code @Autowired}）。
     * 【概念】相依在建構當下就齊備，物件永遠處於可用狀態；測試可用 Mockito 注入假物件。
     */
    public OrderService(RiskService riskService,
                        PricingService pricingService,
                        NotificationService notificationService,
                        OrderRepository orderRepository) {
        this.riskService = riskService;
        this.pricingService = pricingService;
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
    }

    /**
     * 【職責】執行下單編排並回傳成交結果。
     * 【技巧】{@code @Transactional}；通知失敗 catch 後仍回 FILLED（message 註記）。
     * 【概念】編排順序固定；快取／重試／稽核由切面觸發——讀此方法時應想像「周圍還有代理洋蔥」。
     * @param request 下單請求
     * @return 成交結果（通知最終失敗時仍可能為 FILLED）
     */
    @Transactional
    public OrderResult placeOrder(OrderRequest request) {
        // 1. 風控：不通過會拋 RiskRejectedException（被 ExceptionAspect 攔截）。
        riskService.check(request);

        // 2. 報價：相同 symbol 第二次呼叫會命中 CacheAspect 的快取。
        Quote quote = pricingService.getQuote(request.getSymbol());

        // 3. 落庫。
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 12);
        OrderEntity entity = new OrderEntity(orderId, request.getClientOrderId(), request.getSymbol(),
                request.getSide(), request.getQuantity(), quote.mid(), OrderStatus.FILLED, Instant.now());
        orderRepository.save(entity);

        OrderResult result = toResult(entity);

        // 4. 通知：失敗會由 RetryAspect 自動重試；即使最終失敗也不阻斷下單流程。
        try {
            notificationService.notifyFilled(result);
        } catch (NotificationException e) {
            result.setMessage("成交（通知重試後仍失敗，將另行補送）");
        }
        return result;
    }

    /**
     * 【職責】依 orderId 查單。
     * 【技巧】{@code @Transactional(readOnly = true)}；Optional map。
     * 【概念】讀寫分離標註有助於交易管理器優化；不存在回 null 由 Controller 轉 404。
     * @return 結果；不存在為 null
     */
    @Transactional(readOnly = true)
    public OrderResult getOrder(String orderId) {
        return orderRepository.findById(orderId).map(OrderService::toResult).orElse(null);
    }

    /**
     * 【職責】列出全部訂單。
     * 【技巧】readOnly 交易 + Stream map。
     * 【概念】教學用全表列出；正式系統應分頁。
     */
    @Transactional(readOnly = true)
    public List<OrderResult> listOrders() {
        return orderRepository.findAll().stream().map(OrderService::toResult).toList();
    }

    private static OrderResult toResult(OrderEntity entity) {
        return new OrderResult(entity.getOrderId(), entity.getClientOrderId(), entity.getSymbol(),
                entity.getSide(), entity.getQuantity(), entity.getExecutedPrice(),
                entity.getStatus(), "成交");
    }
}
