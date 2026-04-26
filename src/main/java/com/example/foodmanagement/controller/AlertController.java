package com.example.foodmanagement.controller;

import com.example.foodmanagement.model.AlertLog;
import com.example.foodmanagement.service.FoodManagementService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final FoodManagementService foodManagementService;

    public AlertController(FoodManagementService foodManagementService) {
        this.foodManagementService = foodManagementService;
    }

    @GetMapping
    public List<AlertLog> getAlerts(
            @RequestHeader("X-Restaurant-Id") Long restaurantId,
            @RequestParam(required = false) String status
    ) {
        return foodManagementService.getAlerts(restaurantId, status);
    }
}
