package com.trading.app.application;

/**
 * 【職責】風控拒絕下單時拋出，並攜帶規則代碼供錯誤回應。
 * 【技巧】自訂 RuntimeException + {@code ruleCode} 欄位。
 * 【概念】把「哪條規則」與「說明文字」分開，API 可回穩定的 ruleCode（如 R002），前端／監控可依碼處理。
 */
public class RiskRejectedException extends RuntimeException {

    private final String ruleCode;

    /**
     * 【職責】建立含規則代碼的風控例外。
     * 【技巧】message 給人類、ruleCode 給機器。
     * 【概念】GlobalExceptionHandler 讀 ruleCode 填入 ErrorResponse。
     * @param ruleCode 違反的規則代碼（如 R002）
     * @param message  人類可讀說明
     */
    public RiskRejectedException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }

    /** 對應錯誤回應中的規則代碼欄位。 */
    public String getRuleCode() {
        return ruleCode;
    }
}
