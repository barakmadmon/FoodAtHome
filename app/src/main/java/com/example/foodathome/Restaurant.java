package com.example.foodathome;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Restaurant {
    @Exclude
    private String id; // still isnt implemented in firebase
    private String name;
    private String website;
    @Exclude
    private List<RestaurantDish> dishes;
    @Exclude
    private boolean isLoaded = false;


    public Restaurant() { }

    public Restaurant(String name, String website) {
        this.name = name;
        this.website = website;
        this.dishes = new ArrayList<RestaurantDish>();
        this.isLoaded = false;
    }

    public String getName() {
        return name;
    }

    public String getWebsite() {
        return website;
    }

    public String getId() {
        return id;
    }

    public List<RestaurantDish> getDishes() {
        return dishes;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded() {
        isLoaded = true;
    }

    public boolean addDish(RestaurantDish dish) {
        boolean unique = !containDish(dish);
        if (unique)

            dishes.add(dish);

        return !unique;
    }

    public boolean containDish(Dish dish) {
        for (RestaurantDish d : dishes) {
            if (d.getName().equals(dish.getName()))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
