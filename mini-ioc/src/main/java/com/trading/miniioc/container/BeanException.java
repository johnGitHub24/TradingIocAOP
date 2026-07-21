package com.trading.miniioc.container;

/**
 * 【職責】容器在建立或解析 bean 過程中發生錯誤時拋出。
 * 【技巧】繼承 {@link RuntimeException}（非受檢），可帶 message 與 cause。
 * 【概念】把「找不到 bean／循環相依／多候選」收斂成同一例外型別，呼叫端與測試可統一斷言；對照 Spring 的 {@code BeanCreationException} 家族。
 */
public class BeanException extends RuntimeException {

    public BeanException(String message) {
        super(message);
    }

    public BeanException(String message, Throwable cause) {
        super(message, cause);
    }
}
