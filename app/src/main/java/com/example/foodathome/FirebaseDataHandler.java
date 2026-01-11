package com.example.foodathome;

import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseDataHandler {
    static final int BATCH_SIZE = 10;
    private static final String INGREDIENT_COLLECTION = "Ingredients";
    private static final String RECIPE_COLLECTION = "Recipes";
    private static final String Restaurant_COLLECTION = "Restaurants";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static final Semaphore ingredientSem = new Semaphore(0);
    static final Semaphore recipeSem = new Semaphore(0);


    public static void addIngredients(Set<Ingredient> ingredients) {
        Set<String> existingNames = new HashSet<>();
        AtomicInteger completed = new AtomicInteger(0);
        ArrayList<String> names = new ArrayList<>();

        for (Ingredient ing : ingredients) {
            names.add(ing.getName());
        }

        for (int i = 0; i < names.size(); i += 10) {
            List<String>nameChunk = names.subList(i, Math.min(names.size(), i + BATCH_SIZE));

            db.collection(INGREDIENT_COLLECTION)
                    .whereIn("Name",nameChunk)
                    .get()
                    .addOnSuccessListener((QuerySnapshot qs) -> {
                        for (DocumentSnapshot doc : qs.getDocuments()) {
                            Ingredient ing = doc.toObject(Ingredient.class);
                            if (ing != null)
                                existingNames.add(ing.getName());
                        }

                        if (completed.incrementAndGet() == Math.ceil((double) names.size() / BATCH_SIZE)) {
                            WriteBatch batch = db.batch();

                            for (Ingredient ing : ingredients) {
                                if (!existingNames.contains(ing.getName())) {
                                    DocumentReference newDoc = db.collection(INGREDIENT_COLLECTION).document();
                                    batch.set(newDoc, ing);
                                }
                            }
                            batch.commit().addOnSuccessListener(v -> {
                                Log.d("Firebase", "Upload finished!");
                            });
                        }
                    });
        }
    }

    public static void getIngredients(Set<Ingredient> ingredients, boolean recipe) {
        AtomicInteger completed = new AtomicInteger(0);
        ArrayList<String> ingrdientIDS;
        Map<String, Ingredient> ingredientMap = new HashMap<>();

        for (Ingredient ing : ingredients) {
            if(recipe)
                ingredientMap.put(ing.getId(), ing);
            else
                ingredientMap.put(ing.getName(), ing);
            Log.i("myComments", ing.getName());
        }

        ingrdientIDS = new ArrayList<>(ingredientMap.keySet());
        for (int i = 0; i < ingrdientIDS.size(); i += 10) {
            List<String> idChunk = ingrdientIDS.subList(i, Math.min(ingrdientIDS.size(), i + BATCH_SIZE));

            Query q;
//            if(recipe) {
//                q =  db.collection(INGREDIENT_COLLECTION).whereIn(FieldPath.documentId(), idChunk);
//            }
//            else {
//                q =  db.collection(INGREDIENT_COLLECTION).whereIn("Name", idChunk);
//            }
            db = FirebaseFirestore.getInstance();
            q =  db.collection(INGREDIENT_COLLECTION);

            Log.i("myComments", q.toString());

            q.get().addOnSuccessListener((QuerySnapshot qs) -> {
                Log.i("myComments", "getting ingredients");
                for (DocumentSnapshot doc : qs.getDocuments()) {
                    Ingredient temp = doc.toObject(Ingredient.class);
                    temp.setId(doc.getId());

                    if(recipe) {
                        float g = ingredientMap.get(temp.getId()).getAmount();
                        ingredientMap.get(temp.getId()).copy(temp);
                        ingredientMap.get(temp.getId()).setAmount(g);
                    }
                    else {
                        ingredientMap.get(temp.getId()).copy(temp);
                    }
                }

                if (completed.incrementAndGet() == Math.ceil((double) ingrdientIDS.size() / BATCH_SIZE)) {
                    onAllIngredientsResolved(ingredients);
                }
            });
        }

        if (ingrdientIDS.size() == 0) {
            onAllIngredientsResolved(ingredients);
        }
    }

    private static void onAllIngredientsResolved(Set<Ingredient> myIngredients) {
        for (Ingredient ing : myIngredients) {
            if (ing.getId() != null) {
                Log.d("FB", ing.getName() + " → exists, ID assigned: " + ing.getId());
            } else {
                Log.d("FB", ing.getName() + " → does NOT exist in Firestore");
            }
        }
        ingredientSem.release();
    }

    public static void getRecipes(Set<Recipe> recipes) {
        AtomicInteger completed = new AtomicInteger(0);
        ArrayList<String> recipeIDS;
        Map<String, Recipe> recipeMap = new HashMap<>();

        for (Recipe r : recipes) {
            recipeMap.put(r.getId(), r);
        }

        recipeIDS = new ArrayList<>(recipeMap.keySet());
        for (int i = 0; i < recipeIDS.size(); i += 10) {
            List<String> idChunk = recipeIDS.subList(i, Math.min(recipeIDS.size(), i + BATCH_SIZE));

            db.collection(RECIPE_COLLECTION)
                .whereIn(FieldPath.documentId(), idChunk)
                .get().addOnSuccessListener((QuerySnapshot qs) -> {
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        getRecipe(doc);
                    }

                    if (completed.incrementAndGet() == Math.ceil((double) recipeIDS.size() / BATCH_SIZE)) {
                        onAllRecipesResolved(recipes);
                    }
                });
        }
    }

    public static Recipe getRecipe(DocumentSnapshot doc) {
        Recipe recipe = new Recipe(doc.get("Name", String.class));
        recipe.setId(doc.getId());
        recipe.setRecipe(doc.get("Recipe",String.class));
        List<HashMap<String,Object>> ingredients = doc.get("Ingredients", List.class);
        for (HashMap<String,Object> ing: ingredients) {
            Ingredient temp = new Ingredient("");
            temp.setId((String)ing.get("IngredientID"));
            temp.setAmount((float)ing.get("Grams"));
            recipe.addIngredient(temp,(String)ing.get("IngredientAmount"));
        }

        getIngredients(recipe.getIngredients().keySet(),true);
        return recipe;
    }

    private static void onAllRecipesResolved(Set<Recipe> myIngredients) {
        for (Recipe r : myIngredients) {
            if (r.getId() != null) {
                Log.d("FB", r.getName() + " → exists, ID assigned: " + r.getId());
            } else {
                Log.d("FB", r.getName() + " → does NOT exist in Firestore");
            }
        }
        recipeSem.release();
    }
}
