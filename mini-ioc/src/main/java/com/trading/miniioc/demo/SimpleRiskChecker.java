package com.trading.miniioc.demo;

import com.trading.common.OrderRequest;
import com.trading.miniioc.annotation.Component;

/**
 * 【職責】極簡風控：單筆數量須介於 1～上限（含）。
 * 【技巧】{@code @Component}；常數 {@code MAX_QUANTITY}。
 * 【概念】規則集中在一處，OrderPlacer 只問「過不過」——單一職責。
 */
@Component
public class SimpleRiskChecker implements RiskChecker {

    private static final int MAX_QUANTITY = 1000;

    /**
     * 【職責】數量大於 0 且不超過上限則通過。
     * 【技巧】簡單布林條件，無例外。
     * 【概念】mini-ioc 路徑用回傳值表達拒絕；trading-app 的 RiskService 則拋例外——兩種風格都常見。
     */
    @Override
    public boolean approve(OrderRequest request) {
        return request.getQuantity() > 0 && request.getQuantity() <= MAX_QUANTITY;
    }
}
