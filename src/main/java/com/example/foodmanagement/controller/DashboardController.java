package com.example.foodmanagement.controller;

import com.example.foodmanagement.model.DashboardSummary;
import com.example.foodmanagement.service.FoodManagementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final FoodManagementService foodManagementService;

    public DashboardController(FoodManagementService foodManagementService) {
        this.foodManagementService = foodManagementService;
    }

    @GetMapping("/summary")
    public DashboardSummary getSummary(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return foodManagementService.getSummary(restaurantId);
    }
}
