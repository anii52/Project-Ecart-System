package com.ecart.model;

public class Electronics extends Product {
    private String brand;
    public Electronics(int id, String name, double price, int quantity, String brand) {
        super(id, name, price, quantity);
        this.brand = brand;
    }
    @Override
    public String getDetails() { return "Brand: " + brand; }
}