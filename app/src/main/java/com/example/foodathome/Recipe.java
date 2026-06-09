package com.example.foodathome;

import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a recipe, which is a type of dish with ingredients and instructions.
 */
public class Recipe extends Dish {

    private String recipe;
    @Exclude
    private HashMap<Ingredient, String> ingredients;
    private String originRestaurant;
    private String originDish;
    private double originalPrice;
    @Exclude
    private boolean isLoaded = false;

    /**
     * Default constructor for Recipe.
     */
    public Recipe() { }

    /**
     * Constructor for Recipe with a name.
     * @param name The name of the recipe.
     */
    public Recipe(String name) {
        super(name);
        this.ingredients = new HashMap<Ingredient, String>();
        this.originRestaurant = "None"; // Default value
    }

    /**
     * Constructor for Recipe with a name and origin restaurant.
     * @param name The name of the recipe.
     * @param originRestaurant The name of the restaurant the recipe is from.
     */
    public Recipe(String name, String originRestaurant) {
        super(name);
        this.ingredients = new HashMap<Ingredient, String>();
        this.originRestaurant = originRestaurant;
    }

    /**
     * Gets the recipe instructions.
     * @return The recipe instructions.
     */
    public String getRecipe() { return recipe; }

    /**
     * Sets the recipe instructions.
     * @param recipe The recipe instructions.
     */
    public void setRecipe(String recipe) { this.recipe = recipe; }

    /**
     * Gets the ingredients of the recipe.
     * @return A map of ingredients and their amounts.
     */
    public HashMap<Ingredient, String> getIngredients() {
        return ingredients;
    }

    /**
     * Adds an ingredient to the recipe.
     * @param ingredient The ingredient to add.
     * @param amount The amount of the ingredient.
     */
    public void addIngredient(Ingredient ingredient, String amount) {
        this.ingredients.put(ingredient, amount);
    }

    /**
     * Gets the name of the restaurant the recipe is from.
     * @return The name of the origin restaurant.
     */
    public String getOriginRestaurant() {
        return originRestaurant;
    }

    /**
     * Sets the name of the restaurant the recipe is from.
     * @param originRestaurant The name of the origin restaurant.
     */
    public void setOriginRestaurant(String originRestaurant) {
        this.originRestaurant = originRestaurant;
    }

    /**
     * Gets the name of the original dish that inspired the recipe.
     * @return The name of the original dish.
     */
    public String getOriginDish() {
        return originDish;
    }

    /**
     * Sets the name of the original dish that inspired the recipe.
     * @param originDish The name of the original dish.
     */
    public void setOriginDish(String originDish) {
        this.originDish = originDish;
    }

    /**
     * Gets the original price of the dish.
     * @return The original price of the dish.
     */
    public double getOriginalPrice() {
        return originalPrice;
    }

    /**
     * Sets the original price of the dish.
     * @param originalPrice The original price of the dish.
     */
    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }


    /**
     * Gets if the recipe is loaded.
     * @return is the recipe is loaded.
     */
    public Boolean isLoaded() { return isLoaded; }

    /**
     * Sets the recipe load status.
     * @param isLoaded Load status.
     */
    public void setLoaded(Boolean isLoaded) { this.isLoaded = isLoaded; }

    /**
     * Returns a string representation of the recipe.
     * @return A string containing the recipe's name, source, ingredients, total price, and instructions.
     */
    @Override
    public String toString() {
        String recipeStr = "Name: " + name + "\n";
        recipeStr += "Source: " + originRestaurant + "\n";

        recipeStr += "Ingredients: \n";
        for (Map.Entry<Ingredient,String> entry : ingredients.entrySet()) {

            recipeStr += String.format("%s: %s.\t\t",entry.getKey().getName(),entry.getValue());
            if (entry.getKey().calcPrice() != 0)
                recipeStr += String.format("%f\n",entry.getKey().calcPrice());

        }
        recipeStr += String.format("Total: %f\n",totalPrice());

        recipeStr += String.format("Recipe: %s\n",recipe);

        return recipeStr;
    }

    /**
     * Calculates the total price of all ingredients in the recipe.
     * @return The total price.
     */
    public float totalPrice(){
        float total = 0;

        for(Ingredient ingredient: ingredients.keySet()){
            total += ingredient.calcPrice();
        }

        return total;
    }
}