package com.example.foodathome;

import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Restaurant {
    @Exclude
    String id;
    private String name;
    private String website;
    @Exclude
    private List<RestaurantDish> dishes;


}
