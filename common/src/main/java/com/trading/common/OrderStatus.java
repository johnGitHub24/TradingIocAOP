package com.trading.common;

/**
 * 【職責】表達訂單生命週期狀態，供 mini-ioc 與 trading-app 共用。
 * 【技巧】Java {@code enum}；序列化時通常以名稱字串傳遞（JPA {@code EnumType.STRING}）。
 * 【概念】用型別安全的列舉取代魔術字串，編譯期就能擋掉非法狀態；狀態語意集中在此，避免各模組各寫一套常數。
 * 【邊界】不負責狀態轉移規則（誰能從 ACCEPTED 變 FILLED 由 Service 決定）。
 */
public enum OrderStatus {
    /** 已接受、等待撮合。 */
    ACCEPTED,
    /** 已成交。 */
    FILLED,
    /** 遭風控或驗證拒絕。 */
    REJECTED
}
