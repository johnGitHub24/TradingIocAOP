package com.trading.app.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.app.application.OrderService;
import com.trading.app.config.GlobalExceptionHandler;
import com.trading.app.dto.PlaceOrderRequest;
import com.trading.common.OrderResult;
import com.trading.common.OrderStatus;
import com.trading.common.Side;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】驗證 OrderController HTTP 契約（201／400）與驗證錯誤格式。
 * 【技巧】{@code @WebMvcTest} 只載 Web slice；Service MockBean；Import GlobalExceptionHandler。
 * 【概念】Web 層單測不啟動完整 AOP／JPA——狀態碼與 JSON 形狀在此保護。
 */
@Tag("unit")
@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OrderService orderService;

    private PlaceOrderRequest validRequest() {
        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setClientOrderId("C-1");
        req.setSymbol("AAPL");
        req.setSide(Side.BUY);
        req.setQuantity(10);
        req.setPrice(BigDecimal.valueOf(100));
        return req;
    }

    /**
     * CASE ORDER_API_001：合法下單回 201 + FILLED。
     * Given: Service stub 回成交；When: POST /api/v1/orders；Then: 201、status、orderId。
     * 【技巧驗證】薄 Controller 委派與 ResponseEntity CREATED。
     */
    @Test
    void ORDER_API_001_place_returns201() throws Exception {
        OrderResult result = new OrderResult("ORD-1", "C-1", "AAPL", Side.BUY, 10,
                BigDecimal.valueOf(100.5), OrderStatus.FILLED, "成交");
        when(orderService.placeOrder(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FILLED"))
                .andExpect(jsonPath("$.orderId").value("ORD-1"));
    }

    /**
     * CASE ORDER_API_002：驗證失敗回 400 VALIDATION_FAILED。
     * Given: symbol 空、quantity 0；When: POST；Then: 400 + errorCode。
     * 【技巧驗證】@Valid + GlobalExceptionHandler。
     */
    @Test
    void ORDER_API_002_invalidRequest_returns400() throws Exception {
        PlaceOrderRequest invalid = validRequest();
        invalid.setSymbol("");
        invalid.setQuantity(0);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }
}
