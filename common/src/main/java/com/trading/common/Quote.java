package com.trading.common;

import java.math.BigDecimal;

/**
 * 【職責】報價純資料模型（bid／ask），作為 Pricing 回傳值與成交參考價來源。
 * 【技巧】POJO + {@link BigDecimal}；{@link #mid()} 以加減除算出中間價。
 * 【概念】把行情資料獨立成型別，方便 AOP 快取「整個 Quote」而非散落欄位；教學上用來對照 CacheAspect 命中前後是否同一結果。
 * 【邊界】不含即時行情連線；本專案以 hash 推導假報價。
 */
public class Quote {

    private String symbol;
    private BigDecimal bid;
    private BigDecimal ask;

    public Quote() {
    }

    public Quote(String symbol, BigDecimal bid, BigDecimal ask) {
        this.symbol = symbol;
        this.bid = bid;
        this.ask = ask;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    /**
     * 【職責】計算中間價 {@code (bid + ask) / 2}，作為成交參考價。
     * 【技巧】{@link BigDecimal#add}／{@link BigDecimal#divide}；缺值時回 {@code null}。
     * 【概念】業務語意（中間價）放在 domain 方法，呼叫端不必重複公式；與「在 Service 裡手算」相比，語意更靠近資料本身。
     * @return 中間價；bid 或 ask 為 null 時回 null
     */
    public BigDecimal mid() {
        if (bid == null || ask == null) {
            return null;
        }
        return bid.add(ask).divide(BigDecimal.valueOf(2));
    }
}
