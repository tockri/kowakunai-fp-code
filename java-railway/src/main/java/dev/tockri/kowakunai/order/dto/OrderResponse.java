package dev.tockri.kowakunai.order.dto;

public sealed interface OrderResponse permits OrderSuccessResponse, OrderFailureResponse {
    
}
