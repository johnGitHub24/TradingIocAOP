package com.trading.app.dto;

import com.trading.common.OrderRequest;
import com.trading.common.Side;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * 【職責】HTTP 下單請求 DTO：入口格式驗證後轉成 common {@link OrderRequest}。
 * 【技巧】Jakarta Validation 註解（{@code @NotBlank}/{@code @Positive}）；{@link #toDomain()}。
 * 【概念】API 邊界與領域模型分離——驗證規則留在入口，Service 只收乾淨的 domain 物件。
 * 【邊界】不含商業風控（那是 RiskService）。
 */
public class PlaceOrderRequest {

    @NotBlank(message = "clientOrderId 不可為空")
    private String clientOrderId;

    @NotBlank(message = "symbol 不可為空")
    private String symbol;

    @NotNull(message = "side 不可為空")
    private Side side;

    @Positive(message = "quantity 必須大於 0")
    private int quantity;

    @NotNull(message = "price 不可為空")
    @Positive(message = "price 必須大於 0")
    private BigDecimal price;

    /**
     * 【職責】轉成 common 層請求供 Service 使用。
     * 【技巧】手動組裝 POJO（非 MapStruct）。
     * 【概念】DTO → Domain 的防腐：HTTP 形狀變了不必改 Service 簽名。
     */
    public OrderRequest toDomain() {
        return new OrderRequest(clientOrderId, symbol, side, quantity, price);
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
