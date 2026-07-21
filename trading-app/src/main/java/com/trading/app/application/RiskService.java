package com.trading.app.application;

import com.trading.common.OrderRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 【職責】下單前風控：數量與名目金額上限檢查。
 * 【技巧】{@code @Service}；違規拋 {@link RiskRejectedException}（含 ruleCode）。
 * 【概念】失敗用例外表達，讓 ExceptionAspect 與 GlobalExceptionHandler 各司其職（觀測 vs HTTP 422）。對照 mini-ioc 用 boolean 回傳的風格。
 * 【邊界】不做持倉／市場狀態等進階規則。
 */
@Service
public class RiskService {

    static final int MAX_QUANTITY = 1000;
    static final BigDecimal MAX_NOTIONAL = BigDecimal.valueOf(1_000_000);

    /**
     * 【職責】檢查數量與名目金額；不通過即拋例外。
     * 【技巧】名目＝price × quantity；{@link BigDecimal#compareTo}。
     * 【概念】規則集中於此，OrderService 只呼叫一次 check——單一職責。
     * @param request 下單請求
     * @throws RiskRejectedException 規則不通過時
     */
    public void check(OrderRequest request) {
        if (request.getQuantity() > MAX_QUANTITY) {
            throw new RiskRejectedException("R002",
                    "單筆數量 " + request.getQuantity() + " 超過上限 " + MAX_QUANTITY);
        }
        BigDecimal notional = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        if (notional.compareTo(MAX_NOTIONAL) > 0) {
            throw new RiskRejectedException("R003",
                    "委託名目金額 " + notional + " 超過上限 " + MAX_NOTIONAL);
        }
    }
}
