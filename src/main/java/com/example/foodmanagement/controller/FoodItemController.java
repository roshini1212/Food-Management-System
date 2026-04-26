package com.example.foodmanagement.controller;

import com.example.foodmanagement.model.FoodItem;
import com.example.foodmanagement.service.FoodManagementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foods")
public class FoodItemController {

    private final FoodManagementService foodManagementService;

    public FoodItemController(FoodManagementService foodManagementService) {
        this.foodManagementService = foodManagementService;
    }

    @GetMapping
    public List<FoodItem> getFoodItems(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return foodManagementService.getFoodItems(restaurantId);
    }

    @GetMapping("/{id}")
    public FoodItem getFoodItem(@RequestHeader("X-Restaurant-Id") Long restaurantId, @PathVariable Long id) {
        return foodManagementService.getFoodItem(restaurantId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodItem createFoodItem(
            @RequestHeader("X-Restaurant-Id") Long restaurantId,
            @Valid @RequestBody FoodItem foodItem
    ) {
        return foodManagementService.createFoodItem(restaurantId, foodItem);
    }

    @PutMapping("/{id}")
    public FoodItem updateFoodItem(
            @RequestHeader("X-Restaurant-Id") Long restaurantId,
            @PathVariable Long id,
            @Valid @RequestBody FoodItem foodItem
    ) {
        return foodManagementService.updateFoodItem(restaurantId, id, foodItem);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFoodItem(@RequestHeader("X-Restaurant-Id") Long restaurantId, @PathVariable Long id) {
        foodManagementService.deleteFoodItem(restaurantId, id);
    }
}
