package dev.tockri.kowakunai.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderRequestDetail(
        @NotBlank int index,
        @NotBlank(message = "商品名は必須です") String productName,
        @Min(value = 1, message = "数量は1以上でなければなりません") int quantity,
        @Min(value = 0, message = "単価は0以上でなければなりません") int unitPrice) {
}
