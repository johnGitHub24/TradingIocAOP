# Architecture — TradingIocAOP

> 衝突以 [開發專案規格書.md](../開發專案規格書.md) 為準。  
> 詳述：[功能流程說明.md](功能流程說明.md)、[架構學習導引.md](架構學習導引.md)

## Modules (Gradle)

| Module | Responsibility |
|--------|----------------|
| `common` | 純 POJO：`OrderRequest`／`OrderResult`／`Quote`／`Side`／`OrderStatus`（零框架） |
| `mini-ioc` | 手刻 IoC（`MiniApplicationContext`）+ JDK 動態代理 AOP + demo |
| `trading-app` | Spring Boot：DI + 六大 AOP 切面 + REST + JPA |

相依：`common` ← `mini-ioc`；`common` ← `trading-app`（彼此不相依）。

## Layers (trading-app)

| Layer | Package | Responsibility |
|-------|---------|----------------|
| API | `com.trading.app.api` | `OrderController`、`PricingController`、`AspectReportController` |
| Application | `com.trading.app.application` | `OrderService`、`RiskService`、`PricingService`、`NotificationService` |
| Aspect | `com.trading.app.aspect` | Logging／Performance／Exception／Retry／Cache／Audit + `AspectRecorder` |
| Infrastructure | `com.trading.app.infrastructure` | `OrderEntity`、`OrderRepository` |
| Config / DTO | `config`、`dto` | OpenAPI、例外處理、`PlaceOrderRequest` |

## Module map

| Module | Notes |
|--------|-------|
| Place order | 風控 → 報價（Cache）→ 落庫 FILLED → 通知（Retry）→ Audit |
| Pricing | `GET /api/v1/pricing/{symbol}`；第二次起 CacheAspect 命中 |
| Aspect report | `GET /api/v1/aspects/report` 觀測六大切面 |
| mini-ioc demo | `.\gradlew.bat :mini-ioc:run` |

## Runtime

```text
Client
  → Controllers
      → (AOP chain: Logging → Performance → Exception → Retry → Cache)
          → OrderService / PricingService / …
              → OrderRepository → H2 (orders)
```

切面順序（`@Order` 數字越小越外）：Logging(10) → Performance(20) → Exception(30) → Retry(40) → Cache(50) → Audit(60) → 方法本體；Audit 為 `@AfterReturning`。

## Visual maps

| 文件 | 用途 |
|------|------|
| [codeGraphic.html](codeGraphic.html) | Tab：mini-ioc／切面／下單／模組（圖為主） |
