package com.trading.miniioc.demo;

import com.trading.common.OrderRequest;

/**
 * 【職責】風控檢查介面：決定下單請求是否通過。
 * 【技巧】介面方法回傳 boolean；實作由容器注入。
 * 【概念】把「能不能下」從下單編排拆出，方便單獨測試與替換規則。
 */
public interface RiskChecker {

    /**
     * 【職責】檢查請求是否通過風控。
     * 【技巧】純函式風格介面（輸入請求、輸出布林）。
     * 【概念】true＝通過；false＝拒絕。與 trading-app 拋例外的風格不同，刻意對照兩種錯誤處理。
     * @return true 表示通過風控
     */
    boolean approve(OrderRequest request);
}
