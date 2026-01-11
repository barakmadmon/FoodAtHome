package com.example.foodathome;

import static java.lang.System.in;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Recipe {
    @Exclude
    String id;
    private String name;
    private String recipe;
    @Exclude
    private HashMap<Ingredient, String> ingredients;

    public Recipe() { }

    public Recipe(String name) {
        this.name = name;
        this.ingredients = new HashMap<Ingredient, String>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRecipe() { return recipe; }
    public void setRecipe(String recipe) { this.recipe=recipe; }
    public HashMap<Ingredient, String> getIngredients() {
        return ingredients;
    }
    public void addIngredient(Ingredient ingredient,String amount) {
        this.ingredients.put(ingredient,amount);
    }

    @Override
    public String toString() {
        String recipeStr = "Ingridients: \n";

        for (Map.Entry<Ingredient,String> entry : ingredients.entrySet()) {
            recipeStr += String.format("%s: %s.\t\t %f\n",entry.getKey().getName(),entry.getValue(),entry.getKey().calcPrice());
        }
        recipeStr += String.format("total: %f\n",totalPrice());

        recipeStr += String.format("Recipe: %s\n",recipe);

        return recipeStr;
    }

    public float totalPrice(){
        float total = 0;

        for(Ingredient ingredient: ingredients.keySet()){
            total += ingredient.calcPrice();
        }

        return total;
    }
}
