package com.trading.common;

import java.math.BigDecimal;

/**
 * 【職責】下單結果的純資料模型（orderId、成交價、狀態、訊息）。
 * 【技巧】POJO；與 {@link OrderRequest} 對稱，作為 Service／Controller 的回傳契約。
 * 【概念】把「請求」與「結果」拆成兩個型別，API 邊界清楚；同樣零框架相依，跨模組可共用。
 * 【邊界】不負責持久化；trading-app 另有 {@code OrderEntity} 對應資料表。
 */
public class OrderResult {

    private String orderId;
    private String clientOrderId;
    private String symbol;
    private Side side;
    private int quantity;
    private BigDecimal executedPrice;
    private OrderStatus status;
    private String message;

    public OrderResult() {
    }

    public OrderResult(String orderId, String clientOrderId, String symbol, Side side,
                       int quantity, BigDecimal executedPrice, OrderStatus status, String message) {
        this.orderId = orderId;
        this.clientOrderId = clientOrderId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.executedPrice = executedPrice;
        this.status = status;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }

    public void setExecutedPrice(BigDecimal executedPrice) {
        this.executedPrice = executedPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
