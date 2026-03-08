package dev.tockri.kowakunai.order.db;

import org.springframework.data.annotation.Id;

public record OrderDetail(
                @Id Long id,
                String productName,
                int quantity,
                int unitPrice) {
}
