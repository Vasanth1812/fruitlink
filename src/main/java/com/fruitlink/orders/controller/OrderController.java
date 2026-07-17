package com.fruitlink.orders.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.orders.dto.OrderDto.*;
import com.fruitlink.orders.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order lifecycle management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<OrderResponse>> create(@RequestBody CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created", orderService.createOrder(req)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String shopId,
            @RequestParam(required = false) String salesmanId) {
        List<OrderResponse> result;
        if (status != null) result = orderService.getByStatus(status);
        else if (shopId != null) result = orderService.getByShop(shopId);
        else if (salesmanId != null) result = orderService.getBySalesman(salesmanId);
        else result = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrder(id)));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> confirm(
            @PathVariable String id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success("Order confirmed", orderService.confirmOrder(id, user.getUsername())));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String id, @RequestBody UpdateStatusRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", orderService.updateStatus(id, req)));
    }

    @PostMapping("/{id}/repeat")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<OrderResponse>> repeatOrder(
            @PathVariable String id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order repeated", orderService.repeatOrder(id, user.getUsername())));
    }

    @PatchMapping("/{id}/items/{itemId}/packed-qty")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePackedQty(
            @PathVariable String id,
            @PathVariable String itemId,
            @RequestParam java.math.BigDecimal packedQty,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success("Packed quantity updated",
                orderService.updatePackedQty(id, itemId, packedQty, user.getUsername())));
    }
}
