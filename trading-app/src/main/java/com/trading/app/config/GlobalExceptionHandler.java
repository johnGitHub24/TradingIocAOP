package com.trading.app.config;

import com.trading.app.application.RiskRejectedException;
import com.trading.app.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 【職責】全域例外 → 一致的錯誤 JSON 與 HTTP 狀態碼。
 * 【技巧】{@code @RestControllerAdvice} + {@code @ExceptionHandler} 依例外型別分流。
 * 【概念】與 ExceptionAspect 分工：Aspect 做觀測／告警，本類做 HTTP 契約。Controller 不必到處 try-catch。
 * 【邊界】不吞掉未對應的例外語意；最後一個 handler 兜底 500。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 【職責】Bean Validation 失敗 → 400。
     * 【技巧】彙整 fieldErrors 成單一 message。
     * 【概念】入口格式錯誤與業務風控拒絕分開狀態碼，客戶端好處理。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_FAILED", message, null));
    }

    /**
     * 【職責】風控拒絕 → 422，帶 ruleCode。
     * 【技巧】讀取 {@link RiskRejectedException#getRuleCode()}。
     * 【概念】422＝語意上理解請求但無法處理（業務規則），有別於 400 格式錯。
     */
    @ExceptionHandler(RiskRejectedException.class)
    public ResponseEntity<ErrorResponse> handleRisk(RiskRejectedException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("RISK_REJECTED", ex.getMessage(), ex.getRuleCode()));
    }

    /**
     * 【職責】未預期例外 → 500。
     * 【技巧】泛用 {@link Exception} handler 兜底。
     * 【概念】避免堆疊直接洩漏到客戶端；正式環境應再收斂訊息。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", ex.getMessage(), null));
    }
}
