package com.trgmson.backend.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trgmson.backend.dto.driver.DriverResponse;
import com.trgmson.backend.repository.DriverRepository;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverRepository driverRepository;

    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<DriverResponse> getDrivers() {
        return driverRepository.findAll().stream()
                .map(driver -> new DriverResponse(
                        driver.getId(),
                        driver.getUser().getId(),
                        driver.getUser().getFullName(),
                        driver.getUser().getEmail(),
                        driver.getUser().getPhone(),
                        driver.getVehicleType(),
                        driver.getLicensePlate(),
                        driver.getServiceArea(),
                        driver.getMaxLoadKg(),
                        driver.getMaxActiveShipments(),
                        driver.getStatus().name()))
                .toList();
    }
}
