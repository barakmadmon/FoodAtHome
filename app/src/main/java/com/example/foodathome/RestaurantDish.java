package com.example.foodathome;

/**
 * Represents a dish from a restaurant.
 */
public class RestaurantDish extends Dish {
    private String details;
    private String restaurantId;
    private double price;

    /**
     * Default constructor for RestaurantDish.
     */
    public RestaurantDish() {
        super();
    }

    /**
     * Constructor for RestaurantDish with name, details, restaurant ID, and price.
     * @param name The name of the dish.
     * @param details The details of the dish.
     * @param restaurantId The ID of the restaurant the dish belongs to.
     * @param price The price of the dish.
     */
    public RestaurantDish(String name, String details, String restaurantId, double price) {
        super(name);
        this.details = details;
        this.restaurantId = restaurantId;
        this.price = price;
    }

    /**
     * Gets the details of the dish.
     * @return The details of the dish.
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the details of the dish.
     * @param details The details of the dish.
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Gets the ID of the restaurant the dish belongs to.
     * @return The ID of the restaurant.
     */
    public String getRestaurantId() {
        return restaurantId;
    }

    /**
     * Sets the ID of the restaurant the dish belongs to.
     * @param restaurantId The ID of the restaurant.
     */
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    /**
     * Gets the price of the dish.
     * @return The price of the dish.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the price of the dish.
     * @param price The price of the dish.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Returns a string representation of the RestaurantDish.
     * @return A string containing the dish's name, details, and price.
     */
    @Override
    public String toString() {
        String str = "Name: " + (this.name != null ? this.name : "Unknown") + "\n";
        str += "Details: " + (this.details != null && !this.details.isEmpty() ? this.details : "None") + "\n";
        str += "Price: ₪" + String.format("%.2f", price);
        return str;
    }
}