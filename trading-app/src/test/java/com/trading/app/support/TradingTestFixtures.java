package com.trading.app.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.app.dto.PlaceOrderRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 【職責】從 {@code docs/test-data/placeOrder/} 載入下單 fixture JSON。
 * 【技巧】ObjectMapper 反序列化；路徑相對 rootProject（test workingDir 已設定）。
 * 【概念】測試資料外置，CASE id 與檔名對齊，整合測試可重用同一組輸入。
 * 【邊界】僅讀檔；不負責建立 DB 狀態。
 */
public final class TradingTestFixtures {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TradingTestFixtures() {
    }

    /**
     * 【職責】依 caseId 載入 PlaceOrderRequest。
     * 【技巧】{@code docs/test-data/placeOrder/{caseId}.json}。
     * 【概念】失敗時包成 UncheckedIOException 並帶絕對路徑，方便定位缺檔。
     * @param caseId 例如 ORDER-001-SUCCESS
     */
    public static PlaceOrderRequest loadPlaceOrder(String caseId) {
        Path path = Paths.get("docs", "test-data", "placeOrder", caseId + ".json");
        try {
            String json = Files.readString(path);
            return MAPPER.readValue(json, PlaceOrderRequest.class);
        } catch (IOException e) {
            throw new UncheckedIOException("無法載入 fixture：" + path.toAbsolutePath(), e);
        }
    }
}
