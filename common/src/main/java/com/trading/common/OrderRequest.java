package com.trading.common;

import java.math.BigDecimal;

/**
 * 【職責】下單請求的純資料模型，在模組邊界傳遞「要下什麼單」。
 * 【技巧】POJO（無框架註解）；金額用 {@link BigDecimal} 避免浮點誤差。
 * 【概念】零框架相依才能同時被手刻 mini-ioc 與 Spring trading-app 引用——共用 domain 是多模組教學的關鍵。
 * 【邊界】不含 Bean Validation；HTTP 入口驗證在 trading-app 的 DTO。
 */
public class OrderRequest {

    private String clientOrderId;
    private String symbol;
    private Side side;
    private int quantity;
    private BigDecimal price;

    public OrderRequest() {
    }

    public OrderRequest(String clientOrderId, String symbol, Side side, int quantity, BigDecimal price) {
        this.clientOrderId = clientOrderId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
