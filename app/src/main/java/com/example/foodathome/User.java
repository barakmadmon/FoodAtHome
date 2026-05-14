package com.example.foodathome;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user of the application.
 */
public class User {
    private String name;
    private String password;
    private List<String> recipeIds;

    public User() { 
        this.recipeIds = new ArrayList<>();
    }

    /**
     * Constructor for User with name and password.
     * @param name The name of the user.
     * @param password The password of the user.
     */
    public User(String name, String password) {
        this();
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRecipeIds() {
        return recipeIds;
    }

    public void setRecipeIds(List<String> recipeIds) {
        this.recipeIds = recipeIds;
    }
}