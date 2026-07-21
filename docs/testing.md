# Testing and Verification — TradingIocAOP

> 衝突以 [開發專案規格書.md](../開發專案規格書.md) 為準。  
> Case ID 詳表：[測試與CI.md](測試與CI.md)

## Check command

```powershell
.\scripts\check.ps1
# 等同：. .\scripts\env.ps1 ; .\gradlew.bat checkAll
```

（JDK 21；CI：`.github/workflows/ci.yml` 跑 `test` + `integrationTest`。）

## Test layers

| Layer | Tag | Task | 說明 |
|-------|-----|------|------|
| 單元 | `@Tag("unit")` | `gradlew test` | mini-ioc 容器／代理；Risk／Order／Controller |
| 整合 | `@Tag("integration")` | `gradlew integrationTest` | H2 + 切面觀測 + fixture |
| 全部 | — | `gradlew checkAll` | 三模組 |

## Minimum case types

| Type | Coverage |
|------|----------|
| Happy Path | ORDER_INT_001 下單落庫；CACHE／RETRY 整合 |
| Error / edge | RISK_INT_001 拒單；VALIDATION_INT_001；Controller 400 |
| IoC／AOP 原理 | IOC_001～004、AOP_001～003、IOC_INT_001 |

## Key Case IDs（摘要）

| Case | 模組 |
|------|------|
| IOC_*／AOP_* | `mini-ioc` |
| RISK_*／ORDER_*／ORDER_API_* | `trading-app` unit |
| ORDER_INT／RISK_INT／VALIDATION_INT／CACHE_INT／RETRY_INT | `TradingIntegrationTest` |

Fixture：`docs/test-data/placeOrder/`（ORDER-001／002／003）。

## DoD

- [x] Unit tests green（各模組）
- [x] Integration tests green
- [x] `checkAll`／`scripts\check.ps1` 與 CI 對齊
