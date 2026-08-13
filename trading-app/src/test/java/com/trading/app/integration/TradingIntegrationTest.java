package com.trading.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.app.application.NotificationService;
import com.trading.app.application.PricingService;
import com.trading.app.aspect.AspectRecorder;
import com.trading.app.aspect.CacheAspect;
import com.trading.app.dto.PlaceOrderRequest;
import com.trading.app.infrastructure.OrderRepository;
import com.trading.app.support.TradingTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】全鏈路驗證：真實 Spring IoC + 六大切面 + H2 下單與橫切觀測。
 * 【技巧】{@code @SpringBootTest} + MockMvc；BeforeEach 清空 DB／快取／Recorder。
 * 【概念】這是「AOP 看得見」的契約測試——每個 CASE 對應至少一個切面技巧。
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TradingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AspectRecorder recorder;
    @Autowired
    private CacheAspect cacheAspect;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void reset() {
        orderRepository.deleteAll();
        cacheAspect.clear();
        pricingService.resetComputeCount();
        notificationService.reset();
        notificationService.setFailuresBeforeSuccess(1);
        recorder.clear();
    }

    /**
     * CASE ORDER-001 / ORDER_INT_001：下單成功並觸發 Logging／Performance／Audit。
     * Given: fixture ORDER-001-SUCCESS；When: POST orders；Then: 201 FILLED，Recorder 有 log／timing／audit，DB=1。
     * 【技巧驗證】@Around 日誌／計時 + @AfterReturning 稽核。與單元 ORDER-001 同一 Acceptance。
     */
    @Test
    void ORDER_INT_001_placeOrder_succeedsAndTriggersLoggingTimingAudit() throws Exception {
        PlaceOrderRequest request = TradingTestFixtures.loadPlaceOrder("ORDER-001-SUCCESS");

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FILLED"))
                .andExpect(jsonPath("$.executedPrice").exists());

        // 切面 1 LoggingAspect：有記錄進出。
        assertThat(recorder.getLogs()).anyMatch(l -> l.contains("placeOrder"));
        // 切面 2 PerformanceAspect：有記錄耗時。
        assertThat(recorder.getTimings().keySet()).anyMatch(k -> k.contains("placeOrder"));
        // 切面 6 AuditAspect：成交後寫了稽核。
        assertThat(recorder.getAudits()).anyMatch(a -> a.contains("AUDIT"));
        // 落庫確認。
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    /**
     * CASE ORDER-002 / RISK_INT_001：數量超限 422，ExceptionAspect 有紀錄。
     * Given: fixture ORDER-002-RISK_QTY；When: POST；Then: 422 RISK_REJECTED R002，exceptions 含 RiskRejectedException，DB=0。
     * 【技巧驗證】@AfterThrowing 觀測 + GlobalExceptionHandler 422。與單元 ORDER-002 同一 Acceptance。
     */
    @Test
    void RISK_INT_001_quantityOverLimit_returns422AndExceptionAspectRecords() throws Exception {
        PlaceOrderRequest request = TradingTestFixtures.loadPlaceOrder("ORDER-002-RISK_QTY");

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("RISK_REJECTED"))
                .andExpect(jsonPath("$.ruleCode").value("R002"));

        // 切面 3 ExceptionAspect：記錄了風控例外。
        assertThat(recorder.getExceptions()).anyMatch(e -> e.contains("RiskRejectedException"));
        assertThat(orderRepository.count()).isZero();
    }

    /**
     * CASE ORDER-003 / VALIDATION_INT_001：缺 symbol 回 400。
     * Given: fixture ORDER-003-VALIDATION；When: POST；Then: 400 VALIDATION_FAILED。
     * 【技巧驗證】Bean Validation 入口契約。與單元 ORDER-003 同一 Acceptance。
     */
    @Test
    void VALIDATION_INT_001_missingSymbol_returns400() throws Exception {
        PlaceOrderRequest request = TradingTestFixtures.loadPlaceOrder("ORDER-003-VALIDATION");

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    /**
     * CASE CACHE_INT_001：重複報價命中 CacheAspect。
     * Given: 連續 GET 同一 symbol；When: 兩次 pricing；Then: cacheHits≥1 且 computeCount=1。
     * 【技巧驗證】@Around + @Cacheable 短路（不 proceed）。
     */
    @Test
    void CACHE_INT_001_repeatedQuote_hitsCacheAspect() throws Exception {
        mockMvc.perform(get("/api/v1/pricing/TSLA")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/pricing/TSLA")).andExpect(status().isOk());

        // 切面 5 CacheAspect：第二次命中快取。
        assertThat(recorder.getCacheHits()).isGreaterThanOrEqualTo(1);
        // 方法本體只實際執行一次，證明快取生效。
        assertThat(pricingService.getComputeCount()).isEqualTo(1);
    }

    /**
     * CASE RETRY_INT_001：通知重試後訂單仍 FILLED。
     * Given: failuresBeforeSuccess=1；When: 下單；Then: 201 FILLED，retryAttempts 中 notifyFilled ≥2。
     * 【技巧驗證】@Retryable + @Around 重試迴圈。
     */
    @Test
    void RETRY_INT_001_notificationRetried_orderStillFilled() throws Exception {
        PlaceOrderRequest request = TradingTestFixtures.loadPlaceOrder("ORDER-001-SUCCESS");

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FILLED"));

        // 切面 4 RetryAspect：notifyFilled 第一次失敗、重試後成功，總嘗試次數 >= 2。
        assertThat(recorder.getRetryAttempts().entrySet())
                .anyMatch(e -> e.getKey().contains("notifyFilled") && e.getValue() >= 2);
    }
}
