# TradingIocAOP — 專案規則（薄）

繼承：EngineeringOS eos-minimal @ 0.1.5  
公版路徑：`d:\ClaudeCode\EngineeringOS\eos-minimal\`  
權威規格：[開發專案規格書.md](開發專案規格書.md)  
EOS 入口：[TradingIocAOP-SPEC.md](TradingIocAOP-SPEC.md)

## 與公版差異

- Backend port: 8080（`trading-app`）
- Framework: Spring Boot 3.2.2 · Java 21 · 多模組 Gradle（common／mini-ioc／trading-app）
- DB: H2（僅 trading-app）
- 驗證入口：`.\scripts\check.ps1` → `gradlew checkAll`

## 本專案專屬

- 教學雙軌：手刻 IoC／AOP（`mini-ioc`）vs Spring 六大切面（`trading-app`）
- API：`API規格書.md`；Case：`docs/測試與CI.md`
- 觀測：`GET /api/v1/aspects/report`

## 註解深度
- comment_verbosity: **detailed**
- 權威：`EngineeringOS/eos-minimal/knowledge/comments.md` §0／§3b（eos-minimal @ 0.1.5）
- 結構：【職責】【技巧】【概念】；簡單 getter 可併入類別說明


## Git Remote
- 帳號：`johnGitHub24`；一專案一 repo
- 規範：`EngineeringOS/eos-minimal/knowledge/專案上船-GitHub.md`

## 回寫

問題與公版改善建議 → `EngineeringOS/eos-minimal/feedback/SYNC_LOG.md`
