# TradingIocAOP — 啟動與 StartupInfoLogger

> 對齊 TradingCRUD 同套路：`ApplicationReadyEvent` → Console 印常用 URL。  
> 權威開關：`startup.info.*`（`trading-app/src/main/resources/application.yml`）。  
> **瀏覽版：** [啟動與StartupInfoLogger.html](啟動與StartupInfoLogger.html)

---

## 1. 一句話

`bootRun`／IntelliJ 把 `TradingApplication` 拉起來 → Spring 建好 IoC 容器與 AOP 代理 → Tomcat `:8080` 就緒發 `ApplicationReadyEvent` → `StartupInfoLogger` 依 `startup.info.*` 印連結框。

---

## 2. 怎麼啟動

| 入口 | 指令／操作 |
|------|------------|
| IntelliJ | 執行 `TradingApplication`（模組 `trading-app`） |
| Gradle | `.\gradlew.bat :trading-app:bootRun` |
| 對照手刻 | 另開終端：`.\gradlew.bat :mini-ioc:run`（看 `[LOG]`／`[TIMING]`） |

啟動成功後 Console 會出現框線，直接點 URL 即可。

關閉輸出：`startup.info.enabled: false`

---

## 3. Console 會印什麼

| 區塊 | 用途 |
|------|------|
| 健康檢查／應用資訊 | Actuator |
| Swagger UI／OpenAPI | 試 API、看契約 |
| H2 Console | 查 `orders` 表（JDBC 見框內；帳號 `sa`、密碼 `password`） |
| 切面報告 | `GET /api/v1/aspects/report` 看六大切面觀測 |
| 靜態首頁 | 同埠 Vue Demo（`/`） |
| mini-ioc 提示 | 提醒手刻軌如何跑 |

覆寫：`startup.info.project-name`／`frontend`／`h2`／`api-docs`／`extra-paths`／`home-path`

實作：`trading-app/.../config/StartupInfoLogger.java`

---

## 4. 相關技術怎麼用

### 4.1 ApplicationReadyEvent（何時印）

Spring 生命週期：讀 YAML → 建立 Bean（含切面代理）→ 啟動內嵌 Tomcat → **Ready**。  
只有 Ready 之後印 URL，才保證埠已開、健康檢查可用。

### 4.2 Environment 與 `startup.info.*`

`StartupInfoLogger` **不寫死埠號／路徑**，全部 `env.getProperty(...)`。  
改 YAML 即可適配不同專案（CRUD=vite、本專案=static），不必改 Java。

### 4.3 IoC（對照 mini-ioc）

| Spring（trading-app） | 手刻（mini-ioc） |
|----------------------|------------------|
| `ApplicationContext` | `MiniApplicationContext` |
| `@Component` + 建構子注入 | 掃描註解 + 建構子參數解析 |
| 啟動後 Ready 印 URL | `run` 後印攔截器 log |

概念相同：「容器幫你 new、注入相依」；Spring 多了 HTTP／JPA／AOP 織入。

### 4.4 AOP（六大切面怎麼驗證）

1. Swagger 或 Demo 頁 `POST /api/v1/orders` 下一筆單。  
2. `GET /api/v1/aspects/report`：應有 logs、timings、audits 等。  
3. 再 `GET /api/v1/pricing/{symbol}` 兩次：第二次應反映 Cache 命中（report 的 cacheHits）。

切面順序：Logging(10) → Performance(20) → Exception(30) → Retry(40) → Cache(50) → Audit(60)。

### 4.5 Swagger

開 `http://localhost:8080/swagger-ui.html` → 展開 Orders／Aspects／Pricing → Try it out。

### 4.6 H2 Console

1. 開 `http://localhost:8080/h2-console`  
2. JDBC URL 貼 Console 框內字串（預設 `jdbc:h2:mem:tradingdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`）  
3. 使用者 `sa`、密碼 `password`  
4. `SELECT * FROM ORDERS;`

---

## 5. 5 分鐘上手檢查清單

1. `.\gradlew.bat :trading-app:bootRun`（或 IntelliJ Run）  
2. 看 Console 框 → 開 Swagger 或 `/`  
3. 下單 → 開切面報告  
4. （選）H2 查表；（選）另開 `:mini-ioc:run` 對照  

---

## 相關

- 圖解：`docs/codeGraphic.html`  
- 初學者：`docs/初學者學習說明書.md`  
- 設計：`docs/superpowers/specs/2026-07-27-startup-info-logger-design.md`  
- 範本來源：TradingCRUD `StartupInfoLogger`／`docs/啟動與StartupInfoLogger運作流程.md`
