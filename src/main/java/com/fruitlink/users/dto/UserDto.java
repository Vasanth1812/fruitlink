package com.fruitlink.users.dto;

import lombok.Data;

public class UserDto {
    @Data
    public static class UserResponse {
        private String id;
        private String fullName;
        private String phone;
        private String role;
    }
}
