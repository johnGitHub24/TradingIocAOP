package com.trading.app.api;

import com.trading.app.application.OrderService;
import com.trading.app.dto.PlaceOrderRequest;
import com.trading.common.OrderResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 【職責】訂單 REST 入口：收參數、驗證、轉交 Service、組 HTTP 回應。
 * 【技巧】{@code @RestController} + 建構子注入；{@code @Valid} 觸發 Bean Validation。
 * 【概念】薄 Controller——商業規則在 Service，橫切在 Aspect。不負責：風控、落庫、切面邏輯。
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 【職責】注入下單服務。
     * 【技巧】建構子注入。
     * 【概念】Controller 也是 IoC 管理的 bean，測試可用 {@code @WebMvcTest} + MockBean 替換。
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 【職責】下單；成功回 201。
     * 【技巧】DTO → {@code toDomain()}；{@link ResponseEntity} 設 CREATED。
     * 【概念】驗證失敗由 GlobalExceptionHandler 轉 400，風控拒絕轉 422——Controller 保持精簡。
     */
    @PostMapping
    public ResponseEntity<OrderResult> place(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResult result = orderService.placeOrder(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 【職責】依 orderId 查單；不存在回 404。
     * 【技巧】{@code @PathVariable}；null 檢查轉 notFound。
     * 【概念】HTTP 語意（404）留在 Controller，Service 用 null／Optional 表達「沒有」。
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResult> get(@PathVariable String orderId) {
        OrderResult result = orderService.getOrder(orderId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 【職責】列出全部訂單。
     * 【技巧】直接回傳 List，Spring 序列化為 JSON。
     * 【概念】教學用；正式應加分頁參數。
     */
    @GetMapping
    public List<OrderResult> list() {
        return orderService.listOrders();
    }
}
