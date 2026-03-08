package dev.tockri.kowakunai.order.db;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class StockRepository {
    private final Map<Long, Stock> stockMap = Map.of(
            1L, new Stock(1L, 10),
            2L, new Stock(2L, 20),
            3L, new Stock(3L, 1));

    public Optional<Stock> findByProductId(Long productId) {
        return Optional.ofNullable(stockMap.get(productId));
    }
}
