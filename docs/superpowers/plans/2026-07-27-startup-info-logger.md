# StartupInfoLogger + 教學導覽 Implementation Plan

> **For agentic workers:** 依序勾選；本計畫於同意設計後直接執行。

**Goal:** trading-app 啟動印 Console 連結框，並補齊教學導覽說明相關技術怎麼用。

**Architecture:** 複製 TradingCRUD `StartupInfoLogger` 套路，YAML 開關適配本專案（static 前台、無 auth、extra-paths 含切面報告）。文件對齊 APIGatewayMQ／CRUD 的啟動說明風格，內容改寫為 IoC／AOP 雙軌。

**Tech Stack:** Spring Boot 3 · ApplicationReadyEvent · H2 · springdoc · Mermaid HTML

---

### Task 1: StartupInfoLogger + YAML

- [x] 新增 `StartupInfoLogger.java`
- [x] `application.yml` 加 `startup.info.*` 與 `extra-paths`

### Task 2: 啟動說明文件

- [x] `docs/啟動與StartupInfoLogger.md`
- [x] `docs/啟動與StartupInfoLogger.html`

### Task 3: 導覽補強

- [x] `codeGraphic.html` 加「5 分鐘上手」
- [x] `初學者學習說明書.md` 交叉連結

### Task 4: 驗證

- [x] `gradlew :trading-app:compileJava` 通過
