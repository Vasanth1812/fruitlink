package com.fruitlink.settings.dto;

import lombok.Data;
import java.time.Instant;

public class GlobalSettingDto {

    @Data
    public static class SettingRequest {
        private String value;
    }

    @Data
    public static class SettingResponse {
        private String key;
        private String value;
        private Instant updatedAt;
    }
}
