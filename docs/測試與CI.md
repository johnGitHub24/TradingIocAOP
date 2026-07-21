# TradingIocAOP — 測試與 CI

> Case ID 對齊 `開發專案規格書.md` 第 6 章。開發順序：**寫測試 → 實作 → 全綠**。

---

## 測試分層

| 層級 | Tag | 指令 |
|------|-----|------|
| 單元 | `@Tag("unit")` | `.\gradlew.bat test`（各模組 excludeTags integration） |
| 整合 | `@Tag("integration")` | `.\gradlew.bat integrationTest`（includeTags integration） |
| 全部 | — | `.\gradlew.bat checkAll` |

分流靠 JUnit 5 `@Tag` + Gradle `useJUnitPlatform { includeTags / excludeTags }`。

---

## 本機執行

```powershell
. .\scripts\env.ps1
.\gradlew.bat checkAll --console=plain
# 或執行一鍵腳本：
.\scripts\check.ps1
```

測試報告：`{module}/build/reports/tests/`。

---

## Case ID 主表

| Case ID | 模組 | 類型 | 測試類別 |
|---------|------|------|----------|
| IOC_001~004 | mini-ioc | unit | `MiniApplicationContextTest` |
| AOP_001~003 | mini-ioc | unit | `ProxyInterceptorTest` |
| IOC_INT_001 | mini-ioc | integration | `MiniIocIntegrationTest` |
| RISK_001~003 | trading-app | unit | `RiskServiceTest` |
| ORDER_001~003 | trading-app | unit | `OrderServiceTest` |
| ORDER_API_001~002 | trading-app | unit | `OrderControllerTest` |
| ORDER_INT_001 | trading-app | integration | `TradingIntegrationTest` |
| RISK_INT_001 | trading-app | integration | `TradingIntegrationTest` |
| VALIDATION_INT_001 | trading-app | integration | `TradingIntegrationTest` |
| CACHE_INT_001 | trading-app | integration | `TradingIntegrationTest` |
| RETRY_INT_001 | trading-app | integration | `TradingIntegrationTest` |

共 **21** 個 Case。

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
3. 先寫失敗測試（unit / integration），標註 Case ID
4. 實作功能
5. gradlew checkAll 全綠
6. 更新本文件 Case ID 表
```

---

## CI

GitHub Actions（`.github/workflows/ci.yml`）：JDK 21 (temurin)、分別跑 `test` 與 `integrationTest`、上傳測試報告。

---

*最後更新：2026-07-07*
