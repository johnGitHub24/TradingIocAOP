package com.trading.miniioc.container;

import com.trading.miniioc.annotation.Inject;
import com.trading.miniioc.aop.MethodInterceptor;
import com.trading.miniioc.aop.ProxyFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 【職責】極簡 IoC 容器：註冊／掃描元件、建構子依賴注入、單例快取，並可選套上 AOP 代理。
 * 【技巧】反射選建構子 + 遞迴 {@link #getBean}；循環相依用 {@code inCreation} 集合偵測；有介面時走 {@link ProxyFactory}。
 * 【概念】「控制反轉」＝物件不自己 {@code new} 相依，改由容器決定誰注入誰。對照 Spring {@code ApplicationContext}：本類刻意只做單例＋建構子注入，讓核心一眼看懂。
 * 【邊界】不做 scope／setter 注入／循環相依自動破環；無介面的 bean 無法 JDK 代理。
 */
public class MiniApplicationContext {

    private final List<Class<?>> componentClasses = new ArrayList<>();
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final List<MethodInterceptor> interceptors = new ArrayList<>();
    private final Set<Class<?>> inCreation = new LinkedHashSet<>();

    /**
     * 【職責】明確註冊元件類別（不依賴掃描）。
     * 【技巧】可變參數 + fluent {@code return this}，方便鏈式組態。
     * 【概念】手動註冊適合單元測試「只放需要的類」；與 {@link #scan} 並列，對照 Spring 的 {@code @Bean} vs {@code @ComponentScan}。
     * @param classes 要納管的實作類
     * @return this（可繼續鏈式呼叫）
     */
    public MiniApplicationContext register(Class<?>... classes) {
        componentClasses.addAll(Arrays.asList(classes));
        return this;
    }

    /**
     * 【職責】掃描套件並自動註冊所有 {@code @Component}。
     * 【技巧】委派 {@link ClasspathScanner#findComponents}。
     * 【概念】「約定優於設定」：貼上註解就能被發現，減少樣板註冊碼。
     * @param basePackage 掃描起點
     * @return this
     */
    public MiniApplicationContext scan(String basePackage) {
        componentClasses.addAll(ClasspathScanner.findComponents(basePackage));
        return this;
    }

    /**
     * 【職責】登記全域攔截器，套用到所有「有實作介面」的元件。
     * 【技巧】攔截器列表稍後由 {@link ProxyFactory} 包成代理。
     * 【概念】AOP 與 IoC 的接點：容器建立 bean 後再決定是否包裝——業務類別仍無感。
     * @param interceptor 方法攔截器
     * @return this
     */
    public MiniApplicationContext addInterceptor(MethodInterceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    /**
     * 【職責】取得（必要時建立）指定型別的 bean；可為具體類或介面。
     * 【技巧】解析實作 → 單例快取 → 循環偵測 → 反射建構 → 可選代理。
     * 【概念】呼叫端只認介面型別（如 {@code OrderPlacer}），容器負責找實作並注入相依——這就是面向介面的 DI。已被代理時請以介面取得，勿用實作類強轉。
     * @param requiredType 需要的型別（介面或類）
     * @param <T>          bean 型別
     * @return 單例 bean（可能是 JDK 代理）
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        Class<?> implClass = resolveImplementation(requiredType);

        Object existing = singletons.get(implClass);
        if (existing != null) {
            return castOrFail(requiredType, existing);
        }

        if (inCreation.contains(implClass)) {
            throw new BeanException("偵測到循環相依：" + inCreation + " → " + implClass.getName());
        }
        inCreation.add(implClass);

        Object rawInstance = instantiate(implClass);

        inCreation.remove(implClass);

        Object exposed = maybeWrapWithProxy(rawInstance);
        singletons.put(implClass, exposed);
        return castOrFail(requiredType, exposed);
    }

    /**
     * 【職責】回傳已註冊元件數量（測試／診斷用）。
     * 【技巧】讀取內部列表 size。
     * 【概念】用來驗證 scan 是否找到預期數量的 {@code @Component}。
     */
    public int getComponentCount() {
        return componentClasses.size();
    }

    private Class<?> resolveImplementation(Class<?> requiredType) {
        // 具體類別且已註冊 → 直接使用。
        if (componentClasses.contains(requiredType)) {
            return requiredType;
        }
        // 介面或父類別 → 找出唯一一個可指派的元件。
        List<Class<?>> candidates = new ArrayList<>();
        for (Class<?> candidate : componentClasses) {
            if (requiredType.isAssignableFrom(candidate)) {
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()) {
            throw new BeanException("找不到型別為 " + requiredType.getName() + " 的元件");
        }
        if (candidates.size() > 1) {
            throw new BeanException("型別 " + requiredType.getName() + " 有多個候選元件：" + candidates);
        }
        return candidates.get(0);
    }

    private Object instantiate(Class<?> implClass) {
        Constructor<?> constructor = chooseConstructor(implClass);
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            // 控制反轉的核心：每個建構子參數都由容器遞迴解析。
            args[i] = getBean(paramTypes[i]);
        }
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new BeanException("建立 bean 失敗：" + implClass.getName(), e);
        }
    }

    private Constructor<?> chooseConstructor(Class<?> implClass) {
        Constructor<?>[] constructors = implClass.getDeclaredConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        List<Constructor<?>> injectAnnotated = new ArrayList<>();
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Inject.class)) {
                injectAnnotated.add(c);
            }
        }
        if (injectAnnotated.size() == 1) {
            return injectAnnotated.get(0);
        }
        if (injectAnnotated.size() > 1) {
            throw new BeanException(implClass.getName() + " 有多個 @Inject 建構子，無法決定要用哪一個");
        }
        throw new BeanException(implClass.getName()
                + " 有多個建構子但沒有 @Inject 標註，無法決定注入哪一個");
    }

    private Object maybeWrapWithProxy(Object rawInstance) {
        if (interceptors.isEmpty()) {
            return rawInstance;
        }
        if (rawInstance.getClass().getInterfaces().length == 0) {
            // 沒有實作介面的元件無法用 JDK 動態代理，維持原樣。
            return rawInstance;
        }
        return ProxyFactory.createProxy(rawInstance, interceptors);
    }

    @SuppressWarnings("unchecked")
    private <T> T castOrFail(Class<T> requiredType, Object instance) {
        if (!requiredType.isInstance(instance)) {
            throw new BeanException("型別 " + instance.getClass().getName()
                    + " 無法指派給要求的型別 " + requiredType.getName()
                    + "（提示：已被 AOP 代理的元件請以其介面型別取得）");
        }
        return (T) instance;
    }
}
