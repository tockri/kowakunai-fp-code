package dev.tockri.kowakunai.order.dto;

import java.time.LocalDateTime;

public record OrderSuccessResponse(
    long id, String customerName, LocalDateTime orderDateTime, long totalAmount)
    implements OrderResponse {}
