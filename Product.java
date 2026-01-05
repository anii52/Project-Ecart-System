package com.ecart.model;

public abstract class Product {
    protected int id;
    protected String name;
    protected double price;
    protected int quantity;

    public Product(int id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
    public abstract String getDetails();

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
}