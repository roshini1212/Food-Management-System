package com.example.foodmanagement.service;

import com.example.foodmanagement.model.AlertLog;
import com.example.foodmanagement.model.DashboardSummary;
import com.example.foodmanagement.model.FoodItem;
import com.example.foodmanagement.model.Supplier;
import com.example.foodmanagement.repository.AlertRepository;
import com.example.foodmanagement.repository.FoodItemRepository;
import com.example.foodmanagement.repository.SupplierRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FoodManagementService {

    private static final Logger logger = LoggerFactory.getLogger(FoodManagementService.class);

    private final SupplierRepository supplierRepository;
    private final FoodItemRepository foodItemRepository;
    private final AlertRepository alertRepository;

    public FoodManagementService(
            SupplierRepository supplierRepository,
            FoodItemRepository foodItemRepository,
            AlertRepository alertRepository
    ) {
        this.supplierRepository = supplierRepository;
        this.foodItemRepository = foodItemRepository;
        this.alertRepository = alertRepository;
    }

    public List<Supplier> getSuppliers(Long restaurantId) {
        return supplierRepository.findAll(restaurantId);
    }

    public Supplier createSupplier(Long restaurantId, Supplier supplier) {
        logger.info("Creating supplier {}", supplier.getName());
        return supplierRepository.save(restaurantId, supplier);
    }

    public List<FoodItem> getFoodItems(Long restaurantId) {
        return foodItemRepository.findAll(restaurantId);
    }

    public FoodItem getFoodItem(Long restaurantId, Long id) {
        return foodItemRepository.findById(restaurantId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found"));
    }

    public FoodItem createFoodItem(Long restaurantId, FoodItem foodItem) {
        validateSupplier(restaurantId, foodItem.getSupplierId());
        logger.info("Creating food item {} with expiry {}", foodItem.getName(), foodItem.getExpiryDate());
        return foodItemRepository.save(restaurantId, foodItem);
    }

    public FoodItem updateFoodItem(Long restaurantId, Long id, FoodItem foodItem) {
        validateSupplier(restaurantId, foodItem.getSupplierId());
        if (!foodItemRepository.update(restaurantId, id, foodItem)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found");
        }
        logger.info("Updated food item {}", id);
        return getFoodItem(restaurantId, id);
    }

    public void deleteFoodItem(Long restaurantId, Long id) {
        if (!foodItemRepository.delete(restaurantId, id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found");
        }
        logger.info("Deleted food item {}", id);
    }

    public List<AlertLog> getAlerts(Long restaurantId, String status) {
        return alertRepository.findAll(restaurantId, status);
    }

    public DashboardSummary getSummary(Long restaurantId) {
        return new DashboardSummary(
                foodItemRepository.count(restaurantId),
                supplierRepository.count(restaurantId),
                foodItemRepository.countExpiringSoon(restaurantId),
                foodItemRepository.countLowStock(restaurantId),
                alertRepository.countOpenAlerts(restaurantId)
        );
    }

    private void validateSupplier(Long restaurantId, Long supplierId) {
        supplierRepository.findById(restaurantId, supplierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier not found"));
    }
}
