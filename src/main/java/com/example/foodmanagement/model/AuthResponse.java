package com.example.foodmanagement.model;

public class AuthResponse {

    private final Long id;
    private final String restaurantName;
    private final String ownerName;
    private final String email;

    public AuthResponse(Long id, String restaurantName, String ownerName, String email) {
        this.id = id;
        this.restaurantName = restaurantName;
        this.ownerName = ownerName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getEmail() {
        return email;
    }
}
