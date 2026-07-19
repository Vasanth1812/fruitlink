package com.fruitlink.fleet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

public class FleetDto {

    @Data
    public static class CreateDriverRequest {
        @NotBlank(message = "Name is required")
        private String name;
        @NotBlank(message = "Phone is required")
        private String phone;
        private String employeeId;
    }

    @Data
    public static class DriverResponse {
        private String id;
        private String name;
        private String phone;
        private String employeeId;
        private String status;
        private Integer ordersCompleted;
        private Double rating;
    }

    @Data
    public static class CreateVehicleRequest {
        @NotBlank(message = "Registration Number is required")
        private String regNo;
        private String type;
        private Double capacity;
    }

    @Data
    public static class AssignDriverRequest {
        private String driverId;
    }

    @Data
    public static class VehicleResponse {
        private String id;
        private String regNo;
        private String type;
        private Double capacity;
        private String status;
        private String assignedDriverId;
        private String assignedDriverName;
        private LocalDate lastService;
        private LocalDate nextService;
        private Double fuelLevel;
    }
}
