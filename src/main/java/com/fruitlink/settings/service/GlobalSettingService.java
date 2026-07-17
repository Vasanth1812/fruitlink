package com.fruitlink.settings.service;

import com.fruitlink.common.BusinessException;
import com.fruitlink.settings.dto.GlobalSettingDto.SettingRequest;
import com.fruitlink.settings.dto.GlobalSettingDto.SettingResponse;
import com.fruitlink.settings.entity.GlobalSetting;
import com.fruitlink.settings.repository.GlobalSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalSettingService {

    private final GlobalSettingRepository globalSettingRepository;

    public List<SettingResponse> getAllSettings() {
        return globalSettingRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SettingResponse getSetting(String key) {
        GlobalSetting setting = globalSettingRepository.findById(key)
                .orElseThrow(() -> new BusinessException("Global setting not found: " + key));
        return toResponse(setting);
    }

    @Transactional
    public SettingResponse updateSetting(String key, SettingRequest request) {
        GlobalSetting setting = globalSettingRepository.findById(key)
                .orElseThrow(() -> new BusinessException("Global setting not found: " + key));
        
        setting.setValue(request.getValue());
        setting = globalSettingRepository.save(setting);
        return toResponse(setting);
    }

    @Transactional
    public SettingResponse createSetting(String key, SettingRequest request) {
        if (globalSettingRepository.existsById(key)) {
            throw new BusinessException("Global setting already exists: " + key);
        }
        GlobalSetting setting = new GlobalSetting();
        setting.setKey(key);
        setting.setValue(request.getValue());
        setting = globalSettingRepository.save(setting);
        return toResponse(setting);
    }

    private SettingResponse toResponse(GlobalSetting setting) {
        SettingResponse response = new SettingResponse();
        response.setKey(setting.getKey());
        response.setValue(setting.getValue());
        response.setUpdatedAt(setting.getUpdatedAt());
        return response;
    }
}
