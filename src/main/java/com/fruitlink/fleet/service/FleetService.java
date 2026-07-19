package com.fruitlink.fleet.service;

import com.fruitlink.common.BusinessException;
import com.fruitlink.fleet.dto.FleetDto.*;
import com.fruitlink.fleet.entity.Driver;
import com.fruitlink.fleet.entity.Vehicle;
import com.fruitlink.fleet.repository.DriverRepository;
import com.fruitlink.fleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FleetService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public DriverResponse createDriver(CreateDriverRequest req) {
        Driver driver = new Driver();
        driver.setName(req.getName());
        driver.setPhone(req.getPhone());
        driver.setEmployeeId(req.getEmployeeId());
        return toDriverResponse(driverRepository.save(driver));
    }

    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream().map(this::toDriverResponse).collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse createVehicle(CreateVehicleRequest req) {
        Vehicle vehicle = new Vehicle();
        vehicle.setRegNo(req.getRegNo());
        vehicle.setType(req.getType());
        vehicle.setCapacity(req.getCapacity());
        return toVehicleResponse(vehicleRepository.save(vehicle));
    }

    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(this::toVehicleResponse).collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse assignDriverToVehicle(String vehicleId, AssignDriverRequest req) {
        Vehicle vehicle = vehicleRepository.findById(UUID.fromString(vehicleId))
                .orElseThrow(() -> new BusinessException("Vehicle not found"));
        
        Driver driver = null;
        if (req.getDriverId() != null && !req.getDriverId().isEmpty()) {
            driver = driverRepository.findById(UUID.fromString(req.getDriverId()))
                    .orElseThrow(() -> new BusinessException("Driver not found"));
        }
        
        vehicle.setAssignedDriver(driver);
        return toVehicleResponse(vehicleRepository.save(vehicle));
    }

    private DriverResponse toDriverResponse(Driver d) {
        DriverResponse r = new DriverResponse();
        r.setId(d.getId().toString());
        r.setName(d.getName());
        r.setPhone(d.getPhone());
        r.setEmployeeId(d.getEmployeeId());
        r.setStatus(d.getStatus());
        r.setOrdersCompleted(d.getOrdersCompleted());
        r.setRating(d.getRating());
        return r;
    }

    private VehicleResponse toVehicleResponse(Vehicle v) {
        VehicleResponse r = new VehicleResponse();
        r.setId(v.getId().toString());
        r.setRegNo(v.getRegNo());
        r.setType(v.getType());
        r.setCapacity(v.getCapacity());
        r.setStatus(v.getStatus());
        r.setLastService(v.getLastService());
        r.setNextService(v.getNextService());
        r.setFuelLevel(v.getFuelLevel());
        if (v.getAssignedDriver() != null) {
            r.setAssignedDriverId(v.getAssignedDriver().getId().toString());
            r.setAssignedDriverName(v.getAssignedDriver().getName());
        }
        return r;
    }
}
