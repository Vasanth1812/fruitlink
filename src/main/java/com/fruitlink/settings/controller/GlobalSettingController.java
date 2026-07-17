package com.fruitlink.settings.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.settings.dto.GlobalSettingDto.SettingRequest;
import com.fruitlink.settings.dto.GlobalSettingDto.SettingResponse;
import com.fruitlink.settings.service.GlobalSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class GlobalSettingController {

    private final GlobalSettingService globalSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SettingResponse>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success("Settings fetched successfully", globalSettingService.getAllSettings()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SettingResponse>> getSetting(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success("Setting fetched successfully", globalSettingService.getSetting(key)));
    }

    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<SettingResponse>> updateSetting(
            @PathVariable String key,
            @RequestBody SettingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Setting updated successfully", globalSettingService.updateSetting(key, request)));
    }

    @PostMapping("/{key}")
    public ResponseEntity<ApiResponse<SettingResponse>> createSetting(
            @PathVariable String key,
            @RequestBody SettingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Setting created successfully", globalSettingService.createSetting(key, request)));
    }
}
