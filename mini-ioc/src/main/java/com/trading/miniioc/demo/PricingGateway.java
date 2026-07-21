package com.trading.miniioc.demo;

import com.trading.common.Quote;

/**
 * 【職責】報價來源介面，定義「依 symbol 取報價」契約。
 * 【技巧】面向介面程式設計；實作類標 {@code @Component} 供容器注入。
 * 【概念】相依宣告在介面而非具體類——容器才能替換實作，JDK 動態代理也才能生效。沒有介面就無法用本教學的 Proxy。
 */
public interface PricingGateway {

    /**
     * 【職責】依標的取得報價。
     * 【技巧】介面方法；回傳 common 的 {@link Quote}。
     * 【概念】呼叫端不關心報價從哪來（假資料或真行情），只認此契約。
     */
    Quote quote(String symbol);
}
