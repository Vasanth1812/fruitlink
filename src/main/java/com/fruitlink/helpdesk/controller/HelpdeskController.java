package com.fruitlink.helpdesk.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.helpdesk.dto.HelpdeskDto.*;
import com.fruitlink.helpdesk.service.HelpdeskService;
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
@RequestMapping("/helpdesk")
@RequiredArgsConstructor
@Tag(name = "Helpdesk", description = "Support tickets and messages")
public class HelpdeskController {

    private final HelpdeskService helpdeskService;

    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<TicketResponse>> create(
            @RequestBody CreateTicketRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created",
                        helpdeskService.createTicket(req, user.getUsername())));
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        return ResponseEntity.ok(ApiResponse.success(helpdeskService.getAll(status, priority)));
    }

    @GetMapping("/tickets/my")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTickets(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(
                helpdeskService.getMyTickets(user.getUsername())));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(helpdeskService.getTicket(id)));
    }

    @PutMapping("/tickets/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> update(
            @PathVariable String id, @RequestBody UpdateTicketRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Ticket updated",
                helpdeskService.updateTicket(id, req)));
    }

    @PostMapping("/tickets/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> addMessage(
            @PathVariable String id,
            @RequestBody AddMessageRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent",
                        helpdeskService.addMessage(id, req, user.getUsername())));
    }
}
