package com.trading.app.infrastructure;

import com.trading.common.OrderStatus;
import com.trading.common.Side;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 【職責】訂單持久化實體，對應資料表 {@code orders}。
 * 【技巧】JPA {@code @Entity}/{@code @Table}；enum 用 {@code EnumType.STRING}；金額 precision／scale。
 * 【概念】基礎設施模型與 common {@code OrderResult} 分離——DB 形狀可變，API 契約不必跟著抖。
 * 【邊界】不含商業規則；由 Service 組裝後 save。
 */
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id", length = 40)
    private String orderId;

    @Column(name = "client_order_id", length = 64)
    private String clientOrderId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Side side;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "executed_price", precision = 18, scale = 4)
    private BigDecimal executedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrderEntity() {
    }

    /**
     * 【職責】建立完整訂單實體（供 Service 落庫）。
     * 【技巧】全欄位建構子；JPA 另需 protected 無參建構子。
     * 【概念】不可變傾向：欄位無 public setter，減少半成品狀態。
     */
    public OrderEntity(String orderId, String clientOrderId, String symbol, Side side,
                       int quantity, BigDecimal executedPrice, OrderStatus status, Instant createdAt) {
        this.orderId = orderId;
        this.clientOrderId = clientOrderId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.executedPrice = executedPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public Side getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
