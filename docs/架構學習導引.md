# TradingIocAOP — 架構學習導引

> 概念對照 + 面試一句話。權威順序：`開發專案規格書.md` > `API規格書.md` > `功能流程說明.md` > 本文件 > `README.md`。

---

## 一、三層理解

```text
L1 業務：交易下單要先風控、報價、落庫、通知。
L2 架構：Controller → Service → Repository 分層；橫切關注點抽成 AOP 切面。
L3 實作：Spring IoC 容器裝配 bean；Spring AOP 以代理攔截方法。
```

---

## 二、核心概念對照表

| 概念 | 一句話 | 本專案範例 |
|------|--------|-----------|
| 控制反轉 IoC | 物件的建立與裝配交給容器，不由自己掌控 | `MiniApplicationContext` / Spring `ApplicationContext` |
| 依賴注入 DI | 相依透過建構子被動注入 | `OrderService(RiskService, PricingService, ...)` |
| 元件掃描 | 由註解自動找出受管元件 | `ClasspathScanner` / `@ComponentScan` |
| 工廠式 Bean | 用方法產生 bean | `OpenApiConfig.tradingIocAopOpenApi()` |
| AOP | 把橫切邏輯從業務抽離 | 六大 `@Aspect` |
| 動態代理 | 以代理物件攔截方法呼叫 | `ProxyFactory`（JDK proxy）/ Spring 代理 |
| 通知類型 | 攔截的時機點 | `@Around`/`@AfterReturning`/`@AfterThrowing` |

---

## 三、Spring 註解 ↔ 類別對照

| 註解 | 本專案類別 | 作用 |
|------|-----------|------|
| `@SpringBootApplication` | `TradingApplication` | 啟動 + 元件掃描 |
| `@RestController` | `OrderController` 等 | Web 端點 |
| `@Service` | `OrderService` 等 | 商業邏輯元件 |
| `@Component` | `AspectRecorder`、六切面 | 受管元件 |
| `@Aspect` | `LoggingAspect` 等 | 切面 |
| `@Configuration`+`@Bean` | `OpenApiConfig` | 工廠式註冊 |
| `@Repository`(隱含) | `OrderRepository` | 資料存取 |
| `@RestControllerAdvice` | `GlobalExceptionHandler` | 全域例外 |

---

## 四、手刻 vs Spring：同一件事的兩種寫法

```text
mini-ioc                         trading-app (Spring)
────────                         ────────────────────
@Component (自訂)          →      @Component / @Service
MiniApplicationContext     →      ApplicationContext
getBean(Class)             →      建構子 @Autowired
MethodInterceptor          →      @Around 通知
ProxyFactory (JDK proxy)   →      Spring AOP 代理
InterceptorChain           →      advisor chain
```

---

## 五、面試可講的一句話

> 「我用一個 3 模組專案，先手刻一個含反射建構子注入與 JDK 動態代理的迷你 IoC/AOP 容器，再用 Spring 原生 IoC + AOP（Around/AfterReturning/AfterThrowing 六個切面）實作同樣的交易下單情境；因此我能清楚說明容器裝配與代理攔截的原理，而不只是會用註解。」

---

*最後更新：2026-07-07*
