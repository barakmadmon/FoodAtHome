package com.example.foodathome;

import static java.lang.System.in;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Recipe extends Dish {

    private String recipe;
    @Exclude
    private HashMap<Ingredient, String> ingredients;

    public Recipe() { }

    public Recipe(String name) {
        super(name);
        this.ingredients = new HashMap<Ingredient, String>();
    }
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
        String recipeStr = "Name: " + name + "\n";

        recipeStr += "Ingridients: \n";
        for (Map.Entry<Ingredient,String> entry : ingredients.entrySet()) {

            recipeStr += String.format("%s: %s.\t\t",entry.getKey().getName(),entry.getValue());
            if (entry.getKey().calcPrice() != 0)
                recipeStr += String.format("%f\n",entry.getKey().calcPrice());

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
