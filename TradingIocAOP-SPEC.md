# TradingIocAOP Specification

> **EOS 入口規格（英文摘要）。** 領域細節以 [開發專案規格書.md](開發專案規格書.md) 為準。  
> Docs standard: EngineeringOS eos-minimal @ 0.1.10 — `knowledge/documentation.md`

## 0. Document map

| File | Role |
|------|------|
| [開發專案規格書.md](開發專案規格書.md) | **主規格書（權威）** |
| This file | EOS 英文入口／摘要 |
| [API規格書.md](API規格書.md) | REST 契約 |
| [docs/architecture.md](docs/architecture.md) | 模組／分層摘要 |
| [docs/testing.md](docs/testing.md) | 測試／DoD 摘要 |
| [docs/資料庫設計.md](docs/資料庫設計.md) | `orders` 表 |
| [docs/驗證設計.md](docs/驗證設計.md) | DTO／風控錯誤 |
| [docs/testing.md](docs/testing.md) | Case ID、CI |
| [CLAUDE.md](CLAUDE.md) | AI 薄規則 |
| [README.md](README.md) | 快速開始 |

## 1. Scope

- **Purpose:** 以交易下單情境示範 DI／IoC／AOP／多模組；手刻 mini-ioc 與 Spring 對照。
- **Stack:** Java 21 · Spring Boot 3.2.2 · Gradle 多模組 · H2／JPA · Spring AOP · JUnit 5 · Mockito · AssertJ
- **Non-goals:** 真實撮合、Redis／Kafka、認證授權、分散式

## 2. Architecture

三模組：`common`｜`mini-ioc`｜`trading-app`。  
見 [docs/architecture.md](docs/architecture.md)。

## 3. API / Contract

權威：[API規格書.md](API規格書.md)。Base：`http://localhost:8080`。

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/v1/orders` | 下單 → 201 FILLED 或 422 風控 |
| GET | `/api/v1/orders`、`/api/v1/orders/{orderId}` | 列表／單筆 |
| GET | `/api/v1/pricing/{symbol}` | 報價（CacheAspect） |
| GET | `/api/v1/aspects/report` | 切面觀測 |

## 4. Test DoD

- [x] `.\scripts\check.ps1`／`.\gradlew.bat checkAll` green（unit + integration）
- [x] 成對 Case ORDER-001／002／003；公開 Service ≥1 unit；Order API Happy + 錯誤
- [x] 詳表見 [docs/testing.md](docs/testing.md)

本機 Demo：IntelliJ／Gradle `:trading-app:bootRun`（**勿** Application 綠箭）。

## 5. Changelog

| Date | Note |
|------|------|
| 2026-08-13 | 成對 ORDER-001／002／003；check.ps1／bootRun 對齊 EOS 0.1.10 |
| 2026-07-10 | EOS SPEC 入口；摘自 開發專案規格書／API／測試與CI |
