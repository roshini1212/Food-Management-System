package com.example.foodmanagement.model;

public class DashboardSummary {

    private int totalItems;
    private int totalSuppliers;
    private int expiringSoon;
    private int lowStock;
    private int openAlerts;

    public DashboardSummary(int totalItems, int totalSuppliers, int expiringSoon, int lowStock, int openAlerts) {
        this.totalItems = totalItems;
        this.totalSuppliers = totalSuppliers;
        this.expiringSoon = expiringSoon;
        this.lowStock = lowStock;
        this.openAlerts = openAlerts;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalSuppliers() {
        return totalSuppliers;
    }

    public int getExpiringSoon() {
        return expiringSoon;
    }

    public int getLowStock() {
        return lowStock;
    }

    public int getOpenAlerts() {
        return openAlerts;
    }
}
