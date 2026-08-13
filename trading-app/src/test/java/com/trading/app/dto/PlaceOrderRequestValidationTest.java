package com.trading.app.dto;

import com.trading.app.support.TradingTestFixtures;
import com.trading.common.Side;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】單元測試 PlaceOrderRequest 的 Bean Validation 約束。
 * 【技巧】純 Validator（無 Spring）；載入與整合層相同的 fixture。
 * 【概念】入口格式錯誤在進 MockMvc 前就能鎖住；ORDER-003 與整合層同一 Acceptance。
 */
@Tag("unit")
class PlaceOrderRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    /**
     * CASE ORDER-001：合法 fixture 無違規。
     * Given: ORDER-001-SUCCESS；When: validate；Then: violations 為空。
     * 【技巧驗證】與整合 ORDER-001 同一合法輸入。
     */
    @Test
    void ORDER_001_validFixture_hasNoViolations() {
        PlaceOrderRequest request = TradingTestFixtures.loadPlaceOrder("ORDER-001-SUCCESS");

        Set<ConstraintViolation<PlaceOrderRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getSymbol()).isEqualTo("AAPL");
        assertThat(request.getSide()).isEqualTo(Side.BUY);
        assertThat(request.getQuantity()).isEqualTo(10);
        assertThat(request.getPrice()).isEqualByComparingTo(new BigDecimal("190.50"));
    }

    /**
     * CASE ORDER-003：缺 symbol 有違規。
     * Given: fixture ORDER-003-VALIDATION（無 symbol）；When: validate；Then: 違規欄位含 symbol。
     * 【技巧驗證】{@code @NotBlank}；與整合 ORDER-003 同一 Acceptance。
     */
    @Test
    void ORDER_003_missingSymbol_hasViolation() {
        PlaceOrderRequest request = TradingTestFixtures.loadPlaceOrder("ORDER-003-VALIDATION");

        Set<ConstraintViolation<PlaceOrderRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("symbol"));
    }
}
