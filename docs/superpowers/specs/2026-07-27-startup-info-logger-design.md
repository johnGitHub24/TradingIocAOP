# TradingIocAOP — StartupInfoLogger + 教學導覽（設計）

日期：2026-07-27

## 目標

trading-app 啟動後 Console 印常用 URL（對齊 TradingCRUD）；並用文件說明 IoC／AOP／Swagger／H2／切面報告怎麼用。

## 決策

| 項目 | 選擇 |
|------|------|
| 實作位置 | `trading-app/.../config/StartupInfoLogger.java` |
| 觸發 | `ApplicationReadyEvent` |
| YAML | `startup.info.*`；`frontend: static`（同埠 `/`） |
| H2 | 印 JDBC；帳號 `sa`、密碼取自 `spring.datasource.password`（預設 `password`） |
| 教學 | `docs/啟動與StartupInfoLogger.md`＋HTML；`codeGraphic` 加「5 分鐘上手」；初學者說明書交叉連結 |
| mini-ioc | 不實作 Logger；文件對照說明 |

## 不做

- 不改業務／切面語意
- 不改 mini-ioc 啟動流程
- 不引入 Vite／Auth

## 驗收

1. `bootRun`／IntelliJ 起 trading-app → Console 有連結框
2. 框內含 Swagger、H2、aspects/report、靜態首頁
3. 雙擊 HTML 可讀啟動與技術說明
