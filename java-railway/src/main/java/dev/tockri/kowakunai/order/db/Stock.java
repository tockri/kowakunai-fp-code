package dev.tockri.kowakunai.order.db;

import org.springframework.data.annotation.Id;

public record Stock(
    @Id Long productId,
    int quantity
) {
    
}
