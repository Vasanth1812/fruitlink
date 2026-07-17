package com.fruitlink.returns.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.returns.dto.ReturnDto.*;
import com.fruitlink.returns.service.ReturnService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
@Tag(name = "Returns", description = "Return requests and credit notes")
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<ReturnResponse>> create(
            @RequestBody CreateReturnRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Return request created",
                        returnService.createReturn(req, user.getUsername())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String shopId) {
        return ResponseEntity.ok(ApiResponse.success(returnService.getAll(status, shopId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<ReturnResponse>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(returnService.getReturn(id)));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReturnResponse>> review(
            @PathVariable String id, @RequestBody ReviewReturnRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Return reviewed", returnService.reviewReturn(id, req)));
    }

    @PostMapping("/credit-notes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<CreditNoteResponse>> issueCreditNote(
            @RequestBody IssueCreditNoteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Credit note issued", returnService.issueCreditNote(req)));
    }
}
