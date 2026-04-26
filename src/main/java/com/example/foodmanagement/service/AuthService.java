package com.example.foodmanagement.service;

import com.example.foodmanagement.model.AuthRequest;
import com.example.foodmanagement.model.AuthResponse;
import com.example.foodmanagement.model.FoodItem;
import com.example.foodmanagement.model.LoginRequest;
import com.example.foodmanagement.model.RestaurantAccount;
import com.example.foodmanagement.model.Supplier;
import com.example.foodmanagement.repository.FoodItemRepository;
import com.example.foodmanagement.repository.RestaurantAccountRepository;
import com.example.foodmanagement.repository.SupplierRepository;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final RestaurantAccountRepository restaurantAccountRepository;
    private final SupplierRepository supplierRepository;
    private final FoodItemRepository foodItemRepository;

    public AuthService(
            RestaurantAccountRepository restaurantAccountRepository,
            SupplierRepository supplierRepository,
            FoodItemRepository foodItemRepository
    ) {
        this.restaurantAccountRepository = restaurantAccountRepository;
        this.supplierRepository = supplierRepository;
        this.foodItemRepository = foodItemRepository;
    }

    public AuthResponse signup(AuthRequest request) {
        restaurantAccountRepository.findByEmail(request.getEmail()).ifPresent(account -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this email");
        });

        RestaurantAccount account = new RestaurantAccount();
        account.setRestaurantName(request.getRestaurantName().trim());
        account.setOwnerName(request.getOwnerName().trim());
        account.setEmail(request.getEmail().trim().toLowerCase());
        account.setPasswordHash(hashPassword(request.getPassword()));

        RestaurantAccount saved = restaurantAccountRepository.save(account);
        ensureBootstrapData(saved.getId());
        logger.info("Created restaurant account for {}", saved.getEmail());
        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        RestaurantAccount account = restaurantAccountRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!account.getPasswordHash().equals(hashPassword(request.getPassword()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        ensureBootstrapData(account.getId());
        logger.info("Restaurant login for {}", account.getEmail());
        return toResponse(account);
    }

    private AuthResponse toResponse(RestaurantAccount account) {
        return new AuthResponse(
                account.getId(),
                account.getRestaurantName(),
                account.getOwnerName(),
                account.getEmail()
        );
    }

    private void ensureBootstrapData(Long restaurantId) {
        if (supplierRepository.count(restaurantId) > 0) {
            return;
        }

        Supplier supplierOne = new Supplier();
        supplierOne.setName("Fresh Farms Ltd");
        supplierOne.setContactPerson("Anita James");
        supplierOne.setEmail("anita@freshfarms.com");
        supplierOne.setPhone("+1-202-555-0101");
        supplierOne = supplierRepository.save(restaurantId, supplierOne);

        Supplier supplierTwo = new Supplier();
        supplierTwo.setName("Green Valley Produce");
        supplierTwo.setContactPerson("Marcus Lee");
        supplierTwo.setEmail("marcus@greenvalley.com");
        supplierTwo.setPhone("+1-202-555-0134");
        supplierTwo = supplierRepository.save(restaurantId, supplierTwo);

        Supplier supplierThree = new Supplier();
        supplierThree.setName("Sunrise Dairy");
        supplierThree.setContactPerson("Nina Patel");
        supplierThree.setEmail("nina@sunrisedairy.com");
        supplierThree.setPhone("+1-202-555-0178");
        supplierThree = supplierRepository.save(restaurantId, supplierThree);

        seedFoodItem(restaurantId, "Spinach", "Vegetables", supplierOne.getId(), 25, 10, "kg", LocalDate.now().plusDays(4), "SP");
        seedFoodItem(restaurantId, "Milk", "Dairy", supplierThree.getId(), 12, 15, "liters", LocalDate.now().plusDays(2), "MK");
        seedFoodItem(restaurantId, "Tomatoes", "Vegetables", supplierTwo.getId(), 40, 20, "kg", LocalDate.now().plusDays(7), "TM");
        seedFoodItem(restaurantId, "Yogurt", "Dairy", supplierThree.getId(), 8, 10, "boxes", LocalDate.now().plusDays(1), "YG");
    }

    private void seedFoodItem(
            Long restaurantId,
            String name,
            String category,
            Long supplierId,
            int quantity,
            int reorderLevel,
            String unit,
            LocalDate expiryDate,
            String code
    ) {
        FoodItem item = new FoodItem();
        item.setName(name);
        item.setCategory(category);
        item.setSupplierId(supplierId);
        item.setQuantity(quantity);
        item.setReorderLevel(reorderLevel);
        item.setUnit(unit);
        item.setExpiryDate(expiryDate.toString());
        item.setBatchCode("R" + restaurantId + "-" + code);
        foodItemRepository.save(restaurantId, item);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Password hashing unavailable", exception);
        }
    }
}
