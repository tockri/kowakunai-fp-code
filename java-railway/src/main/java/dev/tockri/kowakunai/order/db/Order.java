package dev.tockri.kowakunai.order.db;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("order")
public record Order(
    @Id Long id,
    String customerName,
    LocalDateTime orderTime,
    long totalAmount,
    @MappedCollection(idColumn = "ORDER_ID", keyColumn = "ORDER_KEY") List<OrderDetail> details) {}
