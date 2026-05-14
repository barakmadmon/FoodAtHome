package com.example.foodathome;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    @Exclude
    private String id;
    private String name;
    private String website;
    private LatLng location;
    private List<String> dishIds;

    @Exclude
    private List<RestaurantDish> dishes;
    @Exclude
    private int lastDistance;
    @Exclude
    private boolean isLoaded = false;

    /**
     * Default constructor for Restaurant.
     * Initializes the dishes and dishIds lists.
     */
    public Restaurant() {
        this.dishes = new ArrayList<>();
        this.dishIds = new ArrayList<>();
    }

    /**
     * Constructor for Restaurant with name and website.
     * @param name The name of the restaurant.
     * @param website The website of the restaurant.
     */
    public Restaurant(String name, String website) {
        this();
        this.name = name;
        setWebsite(website);
    }

    /**
     * Gets the ID of the restaurant.
     * @return The ID of the restaurant.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the restaurant.
     * @param id The ID of the restaurant.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the restaurant.
     * @return The name of the restaurant.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the restaurant.
     * @param name The name of the restaurant.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the website of the restaurant.
     * @return The website of the restaurant.
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Sets the website of the restaurant.
     * If the website does not contain a protocol, it adds "https://".
     * @param website The website of the restaurant.
     */
    public void setWebsite(String website) {
        if (website != null && !website.isEmpty()) {
            if (!website.contains("://")) {
                website = "https://" + website;
            }
        }
        this.website = website;
    }

    /**
     * Gets the location of the restaurant.
     * @return The location of the restaurant.
     */
    public LatLng getLocation() {
        return location;
    }

    /**
     * Sets the location of the restaurant.
     * @param location The location of the restaurant.
     */
    public void setLocation(LatLng location) {
        this.location = location;
    }

    /**
     * Gets the list of dish IDs for the restaurant.
     * @return The list of dish IDs.
     */
    public List<String> getDishIds() {
        return dishIds;
    }

    /**
     * Sets the list of dish IDs for the restaurant.
     * @param dishIds The list of dish IDs.
     */
    public void setDishIds(List<String> dishIds) {
        this.dishIds = dishIds;
    }

    /**
     * Gets the list of dishes for the restaurant.
     * @return The list of dishes.
     */
    @Exclude
    public List<RestaurantDish> getDishes() {
        return dishes;
    }

    /**
     * Sets the list of dishes for the restaurant.
     * @param dishes The list of dishes.
     */
    @Exclude
    public void setDishes(List<RestaurantDish> dishes) {
        this.dishes = dishes;
    }

    /**
     * Adds a dish to the restaurant's list of dishes if it doesn't already exist.
     * @param dish The dish to add.
     */
    public void addDish(RestaurantDish dish) {
        if (dish == null) return;
        if (dishes == null) dishes = new ArrayList<>();
        
        boolean exists = false;
        for (RestaurantDish d : dishes) {
            if (d.getName() != null && d.getName().equals(dish.getName())) {
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            dishes.add(dish);
        }
    }

    /**
     * Checks if the restaurant's data has been loaded.
     * @return True if the data is loaded, false otherwise.
     */
    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Sets the loaded status of the restaurant to true.
     */
    public void setLoaded() {
        isLoaded = true;
    }

    /**
     * Calculates the distance between the restaurant and a user's location.
     * @param userLocation The user's location.
     * @return The distance in meters.
     */
    public int calculateDistance(LatLng userLocation) {
        if (this.location == null || userLocation == null) return 0;
        float[] results = new float[1];
        Location.distanceBetween(location.latitude, location.longitude, 
                userLocation.latitude, userLocation.longitude, results);
        lastDistance = (int) results[0];
        return lastDistance;
    }

    @Override
    public String toString() {
        return name + "\t\t" + String.format("%.1f", (float)lastDistance / 1000) + "km";
    }
}