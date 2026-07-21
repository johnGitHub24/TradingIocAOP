package com.trading.miniioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 【職責】標記類別為「受容器管理的元件」，等同 Spring {@code @Component}。
 * 【技巧】自訂註解 + {@code @Retention(RUNTIME)} + {@code @Target(TYPE)}，供掃描器以反射讀取。
 * 【概念】IoC 的第一步是「宣告誰是 bean」：業務類別不自己進容器，而是貼標籤讓容器發現。沒有 RUNTIME 保留，掃描時就看不到註解。
 * 【邊界】不負責注入；注入由 {@link Inject} 與容器建構子解析完成。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {

    /**
     * 【職責】可選的元件名稱；空字串時由容器以類別簡名推導。
     * 【技巧】註解屬性 {@code value()} 預設空字串。
     * 【概念】具名 bean 在多實作時有用；本教學容器主要以型別解析，名稱屬性保留對齊 Spring 習慣。
     */
    String value() default "";
}
