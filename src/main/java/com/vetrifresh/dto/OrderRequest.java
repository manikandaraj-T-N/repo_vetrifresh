package com.vetrifresh.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {

    private String paymentMethod;
    private String shippingName;
    private String shippingAddress;
    private String shippingCity;
    private String shippingPincode;
    private String shippingPhone;
    private String notes;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}