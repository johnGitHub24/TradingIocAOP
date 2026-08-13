package com.trading.app.application;

import com.trading.app.infrastructure.OrderEntity;
import com.trading.app.infrastructure.OrderRepository;
import com.trading.common.OrderRequest;
import com.trading.common.OrderResult;
import com.trading.common.OrderStatus;
import com.trading.common.Quote;
import com.trading.common.Side;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 【職責】驗證 OrderService 編排邏輯（成功／風控中斷／通知失敗仍成交）。
 * 【技巧】Mockito {@code @InjectMocks} 隔離四協作者；不啟動 Spring／AOP。
 * 【概念】單元測「編排」；切面行為留給整合測試——分層驗證。
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private RiskService riskService;
    @Mock
    private PricingService pricingService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest request() {
        return new OrderRequest("C-1", "AAPL", Side.BUY, 10, BigDecimal.valueOf(100));
    }

    /**
     * CASE ORDER-001：合法下單回 FILLED 並呼叫風控／落庫／通知。
     * Given: 報價 stub、save 回傳實體（對齊 fixture ORDER-001-SUCCESS）；When: placeOrder；Then: FILLED、中間價、三協作者被呼叫。
     * 【技巧驗證】編排順序與依賴委派。與整合 ORDER-001 同一 Acceptance。
     */
    @Test
    void ORDER_001_success_returnsFilled() {
        when(pricingService.getQuote("AAPL")).thenReturn(new Quote("AAPL",
                BigDecimal.valueOf(100), BigDecimal.valueOf(101)));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResult result = orderService.placeOrder(request());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(result.getExecutedPrice()).isEqualByComparingTo("100.5");
        verify(riskService).check(any());
        verify(orderRepository).save(any(OrderEntity.class));
        verify(notificationService).notifyFilled(any());
    }

    /**
     * CASE ORDER-002：風控拒絕向上拋且不落庫。
     * Given: check 拋 RiskRejectedException（對齊 fixture ORDER-002-RISK_QTY／R002）；When: placeOrder；Then: 同例外，save never。
     * 【技巧驗證】失敗短路，不繼續編排。與整合 ORDER-002 同一 Acceptance。
     */
    @Test
    void ORDER_002_riskRejected_propagatesAndSkipsPersistence() {
        doThrow(new RiskRejectedException("R002", "拒絕")).when(riskService).check(any());

        assertThatThrownBy(() -> orderService.placeOrder(request()))
                .isInstanceOf(RiskRejectedException.class);

        verify(orderRepository, never()).save(any());
    }

    /**
     * CASE NOTIFY_001：通知失敗仍回 FILLED，message 含通知提示。
     * Given: notifyFilled 拋 NotificationException；When: placeOrder；Then: FILLED 且 message 含「通知」。
     * 【技巧驗證】通知與成交解耦（業務仍成功）。缺欄驗證見 PlaceOrderRequestValidationTest。
     */
    @Test
    void NOTIFY_001_notificationFails_orderStillFilled() {
        when(pricingService.getQuote("AAPL")).thenReturn(new Quote("AAPL",
                BigDecimal.valueOf(100), BigDecimal.valueOf(101)));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new NotificationException("通知失敗")).when(notificationService).notifyFilled(any());

        OrderResult result = orderService.placeOrder(request());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(result.getMessage()).contains("通知");
    }

    /**
     * CASE ORDER_004：查無此單回 null。
     * Given: findById empty；When: getOrder；Then: null（Controller 轉 404）。
     */
    @Test
    void ORDER_004_getOrder_missing_returnsNull() {
        when(orderRepository.findById("ORD-missing")).thenReturn(Optional.empty());

        assertThat(orderService.getOrder("ORD-missing")).isNull();
    }

    /**
     * CASE ORDER_005：列表回空集合。
     * Given: findAll 空；When: listOrders；Then: 空 list。
     */
    @Test
    void ORDER_005_listOrders_empty_returnsEmptyList() {
        when(orderRepository.findAll()).thenReturn(List.of());

        assertThat(orderService.listOrders()).isEmpty();
    }
}
