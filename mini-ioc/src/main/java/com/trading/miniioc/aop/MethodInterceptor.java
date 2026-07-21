package com.trading.miniioc.aop;

/**
 * 【職責】方法攔截器契約：在目標方法前後插入橫切邏輯（對應 Spring AOP {@code @Around}）。
 * 【技巧】{@code @FunctionalInterface}；核心是對 {@link Invocation#proceed()} 的前後包覆。
 * 【概念】「橫切關注點」＝日誌、計時、重試等與業務正交的行為。用攔截器集中處理，業務方法不必到處複製樣板碼；忘記呼叫 {@code proceed()} 等於短路，目標方法不會執行。
 */
@FunctionalInterface
public interface MethodInterceptor {

    /**
     * 【職責】攔截一次方法呼叫，並在適當時機推進呼叫鏈。
     * 【技巧】Around 風格：{@code proceed()} 前回傳前／後／catch 皆可插入邏輯。
     * 【概念】與「在業務方法開頭寫 log」不同：攔截器可掛在多個 bean 上，一處修改、處處生效。
     * @param invocation 進行中的呼叫（含目標、參數、鏈）
     * @return 回傳給呼叫端的結果
     * @throws Throwable 傳遞或轉換後的例外
     */
    Object invoke(Invocation invocation) throws Throwable;
}
