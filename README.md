# TradingIocAOP

以「交易下單／風控」情境，示範 **依賴注入（DI）、控制反轉（IoC）、AOP、模組化架構**。
採「先手刻原理、再用 Spring 對照」的雙軌教學設計。

## 文件入口

| 文件 | 說明 |
|------|------|
| [開發專案規格書.md](開發專案規格書.md) | **主規格書（權威）** |
| [TradingIocAOP-SPEC.md](TradingIocAOP-SPEC.md) | EOS 英文入口／摘要 |
| [API規格書.md](API規格書.md) | REST 契約 |
| [docs/architecture.md](docs/architecture.md) | 模組／分層（摘要） |
| [docs/codeGraphic.html](docs/codeGraphic.html) | Tab 式架構圖（mini-ioc／切面／下單／模組） |
| [docs/testing.md](docs/testing.md) | 測試／DoD（摘要） |
| [docs/測試與CI.md](docs/測試與CI.md) | Case ID + CI |
| [docs/資料庫設計.md](docs/資料庫設計.md) | `orders` 表 |
| [docs/驗證設計.md](docs/驗證設計.md) | DTO／風控錯誤 |
| [docs/功能流程說明.md](docs/功能流程說明.md) | 下單／快取／mini-ioc 流程 |
| [CLAUDE.md](CLAUDE.md) | AI／工程薄規則（繼承 EOS） |

### 學習／教學（非權威）

| 順序 | 文件 | 用途 |
|------|------|------|
| 1 | [docs/初學者學習說明書.md](docs/初學者學習說明書.md) | 第一次上手 |
| 2 | [docs/架構學習導引.md](docs/架構學習導引.md) | 概念地圖 + 面試 |

## 模組

```text
common      純 POJO 領域模型（零框架相依）
mini-ioc    手刻 IoC 容器 + 手刻 AOP（純 Java）
trading-app Spring Boot：IoC/DI + 六大 AOP 切面 + REST API + JPA
```

## 快速開始

```powershell
. .\scripts\env.ps1                    # 設定 JAVA_HOME = JDK 21
.\gradlew.bat checkAll                 # 建置 + 測試三模組
.\gradlew.bat :mini-ioc:run            # 跑手刻容器示範
.\gradlew.bat :trading-app:bootRun     # 啟動 Spring 應用
```

- Swagger UI：http://localhost:8080/swagger-ui.html
- 切面觀測：http://localhost:8080/api/v1/aspects/report
- H2 Console：http://localhost:8080/h2-console

## 六大 AOP 切面

| 切面 | 通知類型 | 作用 |
|------|----------|------|
| LoggingAspect | @Around | 方法進出日誌 |
| PerformanceAspect | @Around | 耗時監控 |
| ExceptionAspect | @AfterThrowing | 例外告警 |
| RetryAspect | @Around + @Retryable | 失敗重試 |
| CacheAspect | @Around + @Cacheable | 結果快取 |
| AuditAspect | @AfterReturning | 成交稽核 |

## 需求：JDK 21

本機若預設 `java` 為舊版，請先執行 `. .\scripts\env.ps1`。

> Docs standard: EngineeringOS eos-minimal @ 0.1.4 — `knowledge/documentation.md`
