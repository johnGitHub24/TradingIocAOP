package com.trading.app.application;

/**
 * 【職責】通知通道失敗時拋出，觸發 RetryAspect 重試。
 * 【技巧】非受檢 {@link RuntimeException}。
 * 【概念】用專用例外型別表達「可重試的暫時失敗」，與風控拒絕等業務例外區隔。
 */
public class NotificationException extends RuntimeException {

    /**
     * 【職責】帶失敗原因建立例外。
     * 【技巧】委派 super(message)。
     * 【概念】訊息會進 log／Recorder，方便觀察第幾次失敗。
     * @param message 失敗原因
     */
    public NotificationException(String message) {
        super(message);
    }
}
