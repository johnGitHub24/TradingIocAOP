package com.trading.app.dto;

import java.time.Instant;

/**
 * 【職責】統一錯誤回應格式（errorCode／message／ruleCode／timestamp）。
 * 【技巧】POJO；建構時自動填 {@link Instant#now()}。
 * 【概念】所有例外路徑回同一形狀，前端與測試可用固定 jsonPath 斷言。
 */
public class ErrorResponse {

    private String errorCode;
    private String message;
    private String ruleCode;
    private Instant timestamp;

    public ErrorResponse() {
    }

    /**
     * 【職責】建立錯誤回應並戳記時間。
     * 【技巧】三參數建構子；timestamp 預設 now。
     * 【概念】ruleCode 可為 null（驗證失敗等無規則碼情境）。
     */
    public ErrorResponse(String errorCode, String message, String ruleCode) {
        this.errorCode = errorCode;
        this.message = message;
        this.ruleCode = ruleCode;
        this.timestamp = Instant.now();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
