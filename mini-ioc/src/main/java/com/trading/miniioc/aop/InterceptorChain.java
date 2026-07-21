package com.trading.miniioc.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 【職責】{@link Invocation} 實作：依序推進攔截器鏈，最後反射呼叫目標方法。
 * 【技巧】每次呼叫新建 chain（自有 {@code currentIndex}）；{@code InvocationTargetException} 解包成真實例外。
 * 【概念】「一呼叫一鏈」避免多執行緒共用索引；解包反射例外讓攔截器看到的是業務例外，而非反射包裝——與 Spring AOP 行為對齊。
 */
public class InterceptorChain implements Invocation {

    private final Object target;
    private final Method method;
    private final Object[] arguments;
    private final List<MethodInterceptor> interceptors;
    private int currentIndex = 0;

    /**
     * 【職責】綁定本次呼叫的目標、方法、參數與攔截器列表。
     * 【技巧】不可變欄位保存呼叫上下文；索引從 0 開始。
     * 【概念】Proxy 的 InvocationHandler 每次方法呼叫都 new 一條鏈，狀態不外洩。
     */
    public InterceptorChain(Object target, Method method, Object[] arguments,
                            List<MethodInterceptor> interceptors) {
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.interceptors = interceptors;
    }

    /**
     * 【職責】推進到下一個攔截器，或執行目標方法。
     * 【技巧】索引遞增後 {@code interceptor.invoke(this)}；鏈盡則 {@code method.invoke}。
     * 【概念】遞迴式「把 this 傳給下一個」形成巢狀 Around；登記順序＝外到內的包覆順序。
     */
    @Override
    public Object proceed() throws Throwable {
        if (currentIndex < interceptors.size()) {
            MethodInterceptor next = interceptors.get(currentIndex++);
            return next.invoke(this);
        }
        // 所有攔截器都跑完了，呼叫真正的目標方法。
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException e) {
            // 解包反射例外，讓攔截器與呼叫端看到的是目標方法真正拋出的例外。
            throw e.getTargetException();
        }
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object getTarget() {
        return target;
    }
}
