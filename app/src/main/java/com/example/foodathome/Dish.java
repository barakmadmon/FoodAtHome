package com.example.foodathome;

import com.google.firebase.firestore.Exclude;

public class Dish {
    @Exclude
    protected String id;
    protected String name;

    public Dish() { }

    public Dish(String name) {
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
