package com.trading.miniioc.aop;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 【職責】以 JDK 動態代理為目標物件套上攔截器鏈（對應 Spring 對介面 bean 的 JDK proxy）。
 * 【技巧】{@link Proxy#newProxyInstance}；InvocationHandler 內建立 {@link InterceptorChain} 並 {@code proceed()}。
 * 【概念】呼叫端拿到的是「看起來像介面」的代理；真正邏輯在 target。Spring 對類別另用 CGLIB——本教學只做介面半邊，刻意凸顯「為什麼要面向介面」。
 * 【邊界】目標必須實作至少一個介面；否則拋 {@link IllegalArgumentException}。
 */
public final class ProxyFactory {

    private ProxyFactory() {
    }

    /**
     * 【職責】為 {@code target} 產生代理，介面方法呼叫會依序經過 {@code interceptors}。
     * 【技巧】讀取 {@code getInterfaces()}；Lambda 當 InvocationHandler。
     * 【概念】業務程式碼零改動即可加日誌／計時——這就是 AOP「非侵入」的體感。
     * @param target       原始目標（須實作介面）
     * @param interceptors 依序套用的攔截器（先登記者為外層）
     * @return 實作相同介面的代理物件
     */
    public static Object createProxy(Object target, List<MethodInterceptor> interceptors) {
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalArgumentException(
                    "無法代理未實作任何介面的類別：" + target.getClass().getName());
        }
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                interfaces,
                (proxy, method, args) -> {
                    InterceptorChain chain = new InterceptorChain(target, method, args, interceptors);
                    return chain.proceed();
                });
    }
}
