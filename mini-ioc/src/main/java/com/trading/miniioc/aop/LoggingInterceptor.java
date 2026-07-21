package com.trading.miniioc.aop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 【職責】記錄方法進出與參數／例外（對應 trading-app {@code LoggingAspect}）。
 * 【技巧】Around：{@code proceed()} 前記錄進入、成功後記錄離開、catch 後記錄例外再拋出。
 * 【概念】日誌是典型橫切關注點——不應散落在每個業務方法。收集到 {@link #getLogs()} 讓測試可斷言「代理真的包到了」。
 */
public class LoggingInterceptor implements MethodInterceptor {

    private final List<String> logs = new ArrayList<>();

    /**
     * 【職責】包覆一次呼叫並寫入進出日誌。
     * 【技巧】try／catch 包 {@code proceed()}；失敗仍向上拋，不吞例外。
     * 【概念】與 {@code @Around} 相同心智模型：攔截器決定「要不要、何時」呼叫目標。
     */
    @Override
    public Object invoke(Invocation invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        String args = Arrays.toString(invocation.getArguments());
        record("→ 進入 " + method + " 參數=" + args);
        try {
            Object result = invocation.proceed();
            record("← 離開 " + method + " 回傳=" + result);
            return result;
        } catch (Throwable t) {
            record("✗ " + method + " 拋出例外=" + t.getClass().getSimpleName());
            throw t;
        }
    }

    private void record(String line) {
        logs.add(line);
        System.out.println("[LOG] " + line);
    }

    /** 已收集的日誌列（測試斷言用）。 */
    public List<String> getLogs() {
        return logs;
    }
}
