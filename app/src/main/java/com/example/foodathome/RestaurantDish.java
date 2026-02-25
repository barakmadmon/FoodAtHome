package com.example.foodathome;

import java.util.Map;

public class RestaurantDish extends Dish {
    private String details;
    private String restaurantId;

    public RestaurantDish(String name,String details, String restaurantId) {
        super(name);
        this.details = details;
        this.restaurantId = restaurantId;
    }

    public String getDetails() {
        return details;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    @Override
    public String toString() {
        String str = "name: " + this.name + "\n";
        str += "detais: " + (!this.details.isEmpty() ? this.details : "None");

        return str;
    }

    
}
