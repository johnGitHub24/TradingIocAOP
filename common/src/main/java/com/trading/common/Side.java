package com.trading.common;

/**
 * 【職責】表達買賣方向。
 * 【技巧】Java {@code enum}，與訂單／實體欄位共用同一型別。
 * 【概念】方向只有 BUY／SELL 兩種合法值；用 enum 比字串更不易拼錯，也方便 switch／序列化。
 */
public enum Side {
    BUY,
    SELL
}
