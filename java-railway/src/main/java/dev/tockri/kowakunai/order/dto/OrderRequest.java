package dev.tockri.kowakunai.order.dto;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record OrderRequest(
        @NotBlank(message = "顧客名は必須です") String customerName,
        @NotEmpty(message = "注文日時は必須です") LocalDateTime orderDateTime,
        @NotEmpty(message = "注文詳細は必須です") List<OrderRequestDetail> details) {
}
