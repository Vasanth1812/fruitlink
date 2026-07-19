package com.fruitlink.audit.controller;

import com.fruitlink.audit.dto.AuditDto.AuditLogResponse;
import com.fruitlink.audit.entity.AuditLog;
import com.fruitlink.audit.repository.AuditLogRepository;
import com.fruitlink.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit logging operations")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        
        Page<AuditLog> logPage = auditLogRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        
        List<AuditLogResponse> responses = logPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse r = new AuditLogResponse();
        r.setId(log.getId().toString());
        if (log.getActor() != null) {
            r.setUserId(log.getActor().getId().toString());
            r.setUserName(log.getActor().getFullName());
        }
        r.setAction(log.getAction());
        r.setModule(log.getModule());
        r.setDetails(log.getAfterState() != null ? log.getAfterState() : log.getBeforeState());
        r.setTimestamp(log.getCreatedAt());
        return r;
    }
}
