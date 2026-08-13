# TradingIocAOP

以「交易下單／風控」情境，示範 **依賴注入（DI）、控制反轉（IoC）、AOP、模組化架構**。
採「先手刻原理、再用 Spring 對照」的雙軌教學設計。

## 文件入口

單一入口：本 README。衝突以主規格為準。

| 文件 | 說明 |
|------|------|
| [開發專案規格書.md](開發專案規格書.md) | **主規格（權威）** |
| [API規格書.md](API規格書.md) | API 契約 |
| [docs/architecture.md](docs/architecture.md) | 分層與模組 |
| [docs/codeGraphic.html](docs/codeGraphic.html) | 架構圖（非權威） |
| [docs/testing.md](docs/testing.md) | 測試／Case／check |
| [docs/資料庫設計.md](docs/資料庫設計.md) | 資料庫 |
| [docs/驗證設計.md](docs/驗證設計.md) | 驗證／權限 |
| [CLAUDE.md](CLAUDE.md) | AI 薄規則 |
| [scripts/README.md](scripts/README.md) | 驗證／啟動腳本 |

## 模組

```text
common      純 POJO 領域模型（零框架相依）
mini-ioc    手刻 IoC 容器 + 手刻 AOP（純 Java）
trading-app Spring Boot：IoC/DI + 六大 AOP 切面 + REST API + JPA
```

## 快速開始

驗證（JDK 21；可先 `. .\scripts\env.ps1`）：

```powershell
.\scripts\check.ps1
```

啟動（IntelliJ／終端請用 Gradle **`:trading-app:bootRun`**；**不要**對 `TradingApplication` 按綠箭頭）：

```powershell
.\gradlew.bat :trading-app:bootRun
.\gradlew.bat :mini-ioc:run            # 手刻容器對照（另開終端）
```

- Swagger UI：http://localhost:8080/swagger-ui.html
- 切面觀測：http://localhost:8080/api/v1/aspects/report
- H2 Console：http://localhost:8080/h2-console

## 驗證指令

| 指令 | 用途 |
|------|------|
| `.\scripts\check.ps1` | 閘門：載入 JDK 21 後 `gradlew checkAll`（unit + integration） |
| `.\gradlew.bat checkAll` | 等同三模組 `check` |
| `.\gradlew.bat :trading-app:bootRun` | 本機 Demo（IntelliJ Gradle 同任務） |

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

> Docs standard: EngineeringOS eos-minimal @ 0.1.10 — `knowledge/documentation.md`

