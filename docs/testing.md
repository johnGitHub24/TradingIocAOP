# TradingIocAOP — 測試與 CI

> Case ID 對齊 `開發專案規格書.md` 第 6 章。開發順序：**寫測試 → 實作 → 全綠**。  
> 成對規範：`EngineeringOS/eos-minimal/knowledge/testing.md`（ORDER-001／002／003 雙層同一 Acceptance）。

---

## 測試分層

| 層級 | Tag | 指令 |
|------|-----|------|
| 單元 | `@Tag("unit")` | `.\gradlew.bat test`（各模組 excludeTags integration） |
| 整合 | `@Tag("integration")` | `.\gradlew.bat integrationTest`（includeTags integration） |
| 全部 | — | `.\scripts\check.ps1` → `gradlew checkAll` |

分流靠 JUnit 5 `@Tag` + Gradle `useJUnitPlatform { includeTags / excludeTags }`。

---

## 本機執行

```powershell
. .\scripts\env.ps1
.\scripts\check.ps1
# 等同：
.\gradlew.bat checkAll --console=plain
```

測試報告：`{module}/build/reports/tests/`。

---

## Case ID 主表

| Case ID | 模組 | 類型 | 測試類別 |
|---------|------|------|----------|
| IOC_001~004 | mini-ioc | unit | `MiniApplicationContextTest` |
| AOP_001~003 | mini-ioc | unit | `ProxyInterceptorTest` |
| MINI_ORDER_001~002 | mini-ioc | unit | `SimpleOrderPlacerTest` |
| MINI_RISK_001 | mini-ioc | unit | `SimpleRiskCheckerTest` |
| MINI_PRICE_001 | mini-ioc | unit | `SimplePricingGatewayTest` |
| IOC_INT_001 | mini-ioc | integration | `MiniIocIntegrationTest` |
| RISK_001~003 | trading-app | unit | `RiskServiceTest`（RISK_002 配對 ORDER-002） |
| ORDER-001 | trading-app | unit + integration | `OrderServiceTest`／`PlaceOrderRequestValidationTest` ↔ `TradingIntegrationTest` |
| ORDER-002 | trading-app | unit + integration | `OrderServiceTest`／`RiskServiceTest` ↔ `TradingIntegrationTest` 422 |
| ORDER-003 | trading-app | unit + integration | `PlaceOrderRequestValidationTest` ↔ `TradingIntegrationTest` 400 |
| ORDER_004~005 | trading-app | unit | `OrderServiceTest`（get／list） |
| ORDER_API_001~003 | trading-app | unit（WebMvc slice） | `OrderControllerTest`（201／400／422） |
| PRICE_001 | trading-app | unit | `PricingServiceTest` |
| NOTIFY_001~002 | trading-app | unit | `OrderServiceTest`／`NotificationServiceTest` |
| CACHE_INT_001 | trading-app | integration | `TradingIntegrationTest` |
| RETRY_INT_001 | trading-app | integration | `TradingIntegrationTest` |

ORDER-001／002／003 為 hyphenated 成對 ID（掃描器 `scan-paired-tests.ps1` 可對）。

---

## Fixture

- 位置：`docs/test-data/placeOrder/{ORDER-001-SUCCESS, ORDER-002-RISK_QTY, ORDER-003-VALIDATION}.json`
- 載入：`TradingTestFixtures.loadPlaceOrder(caseId)`
- test 任務設 `workingDir = rootProject.projectDir`，故相對路徑成立。

---

## 擴充 SOP

```text
1. 若改資料表 → 更新 OrderEntity 與本文件第 5 章
2. docs/test-data/ 新增 fixture JSON
3. 先寫失敗測試（unit / integration），標註同一 Case ID
4. 實作功能
5. .\scripts\check.ps1 全綠
6. 更新本文件 Case ID 表
```

---

## CI

GitHub Actions（`.github/workflows/ci.yml`）：JDK 21 (temurin)、分別跑 `test` 與 `integrationTest`、上傳測試報告。

---

*最後更新：2026-08-13*
