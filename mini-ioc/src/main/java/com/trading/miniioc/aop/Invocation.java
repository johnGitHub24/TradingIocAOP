package com.trading.miniioc.aop;

import java.lang.reflect.Method;

/**
 * 【職責】代表一次「進行中的方法呼叫」，串接攔截器與目標方法（對應 Spring {@code ProceedingJoinPoint}）。
 * 【技巧】{@link #proceed()} 推進鏈；另提供 method／args／target 供攔截器讀取。
 * 【概念】攔截器不直接互叫，而是透過 Invocation 形成「洋蔥」：外層 before → 內層 → 目標 → 內層 after → 外層 after。這是 AOP 攔截器鏈的標準模型。
 */
public interface Invocation {

    /**
     * 【職責】推進呼叫鏈：下一個攔截器，或真正呼叫目標方法。
     * 【技巧】由 {@link InterceptorChain} 實作索引遞增。
     * 【概念】每個攔截器只關心「自己前後要做什麼」，不必知道後面還有誰——職責分離。
     * @return 目標方法（或後續攔截器）的回傳值
     * @throws Throwable 目標方法拋出的任何例外
     */
    Object proceed() throws Throwable;

    /** 被呼叫的目標方法。 */
    Method getMethod();

    /** 呼叫時傳入的參數。 */
    Object[] getArguments();

    /** 未被代理包裝的原始目標物件。 */
    Object getTarget();
}
