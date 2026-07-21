# TradingIocAOP — API 規格書

> 對齊 `開發專案規格書.md` 第 4、5 章。
> Base URL：`http://localhost:8080`　·　Swagger UI：`/swagger-ui.html`

---

## POST /api/v1/orders

下單（風控 → 報價 → 落庫 → 通知）。

### Request

```json
{
  "clientOrderId": "C-1001",
  "symbol": "AAPL",
  "side": "BUY",
  "quantity": 10,
  "price": 190.50
}
```

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| clientOrderId | string | ✅ | 客戶端訂單鍵 |
| symbol | string | ✅ | 商品代碼 |
| side | enum | ✅ | `BUY` / `SELL` |
| quantity | int | ✅ | 需 > 0 |
| price | number | ✅ | 需 > 0 |

### Response 201

```json
{
  "orderId": "ORD-3f2a1b9c4d5e",
  "clientOrderId": "C-1001",
  "symbol": "AAPL",
  "side": "BUY",
  "quantity": 10,
  "executedPrice": 190.25,
  "status": "FILLED",
  "message": "成交"
}
```

---

## GET /api/v1/orders/{orderId}

查詢單一訂單。存在回 `200`，不存在回 `404`。

## GET /api/v1/orders

列出所有訂單（回傳陣列）。

---

## GET /api/v1/pricing/{symbol}

取得報價。連續查詢同一 symbol 第二次起命中 `CacheAspect`。

### Response 200

```json
{
  "symbol": "AAPL",
  "bid": 190.00,
  "ask": 190.50
}
```

---

## GET /api/v1/aspects/report

回傳六大切面收集到的觀測結果（教學/驗證用）。

### Response 200

```json
{
  "logs": ["→ 進入 OrderService.placeOrder(..) 參數=[...]", "← 離開 ..."],
  "timings": { "OrderService.placeOrder(..)": 12 },
  "audits": ["AUDIT orderId=ORD-... status=FILLED"],
  "exceptions": [],
  "cacheHits": 1,
  "cacheMisses": 1,
  "retryAttempts": { "NotificationService.notifyFilled(..)": 2 }
}
```

---

## 錯誤碼

| HTTP | errorCode | ruleCode | 情境 |
|------|-----------|----------|------|
| 400 | VALIDATION_FAILED | — | 欄位驗證失敗（缺 symbol、quantity ≤ 0 等） |
| 422 | RISK_REJECTED | R002 | 單筆數量超過上限 |
| 422 | RISK_REJECTED | R003 | 委託名目金額超過上限 |
| 404 | — | — | 訂單不存在 |
| 500 | INTERNAL_ERROR | — | 未預期錯誤 |

錯誤回應格式：

```json
{
  "errorCode": "RISK_REJECTED",
  "message": "單筆數量 5000 超過上限 1000",
  "ruleCode": "R002",
  "timestamp": "2026-07-07T09:58:56Z"
}
```

---

*最後更新：2026-07-07 | 技術棧：Spring Boot 3.2.2 · springdoc-openapi 2.3.0*
