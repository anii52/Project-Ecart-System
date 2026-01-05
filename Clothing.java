package com.ecart.model;

public class Clothing extends Product {
    private String size;
    public Clothing(int id, String name, double price, int quantity, String size) {
        super(id, name, price, quantity);
        this.size = size;
    }
    @Override
    public String getDetails() { return "Size: " + size; }
}