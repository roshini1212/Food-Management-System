package com.example.foodmanagement.controller;

import com.example.foodmanagement.model.Supplier;
import com.example.foodmanagement.service.FoodManagementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final FoodManagementService foodManagementService;

    public SupplierController(FoodManagementService foodManagementService) {
        this.foodManagementService = foodManagementService;
    }

    @GetMapping
    public List<Supplier> getSuppliers(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return foodManagementService.getSuppliers(restaurantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Supplier createSupplier(
            @RequestHeader("X-Restaurant-Id") Long restaurantId,
            @Valid @RequestBody Supplier supplier
    ) {
        return foodManagementService.createSupplier(restaurantId, supplier);
    }
}
