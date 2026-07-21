package com.trading.app.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 【職責】訂單資料存取（查詢／寫入），不含商業規則。
 * 【技巧】Spring Data JPA：繼承 {@link JpaRepository} 即得 CRUD；方法名推導查詢。
 * 【概念】Repository 是基礎設施邊界——Service 依賴介面，測試可 Mock。對照 mini-ioc 示範路徑無持久化。
 */
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    /**
     * 【職責】依客戶端訂單號查單。
     * 【技巧】Spring Data 方法名查詢 {@code findByClientOrderId}。
     * 【概念】不必手寫 JPQL；命名約定即查詢意圖。
     */
    Optional<OrderEntity> findByClientOrderId(String clientOrderId);
}
