package dev.tockri.kowakunai.order.dto;

import java.util.List;

public record OrderFailureResponse(
    List<String> errors
) implements OrderResponse {
    
}
