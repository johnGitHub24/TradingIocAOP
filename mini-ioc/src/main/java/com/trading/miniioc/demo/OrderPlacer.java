package com.trading.miniioc.demo;

import com.trading.common.OrderRequest;
import com.trading.common.OrderResult;

/**
 * 【職責】下單編排介面：接受請求、回傳結果。
 * 【技巧】介面作為容器解析與 JDK 代理的型別錨點。
 * 【概念】呼叫端（Demo／測試）只依賴此介面，才能拿到被攔截器包裝的代理。
 */
public interface OrderPlacer {

    /**
     * 【職責】執行一次下單編排。
     * 【技巧】輸入／輸出皆為 common 純模型。
     * 【概念】編排細節在實作類；介面保持穩定契約。
     */
    OrderResult place(OrderRequest request);
}
