package com.example.foodathome;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Fragment responsible for displaying a recipe, either by generating it via AI
 * or by loading a previously saved version.
 */
public class RecipeFragment extends Fragment {
    private static final Logger log = LoggerFactory.getLogger(RecipeFragment.class);
    private Button saveRecipeBT;
    private TextView recipeTV;
    private ListView ingredientsListView;
    private TextView recipeTotalPriceTV;
    private TextView originalDishTotalPriceTV;
    private Recipe currentRecipe;
    static final Semaphore aiSem = new Semaphore(0);

    private String dishToLoad = null;
    private String originRestaurant = null;
    private String detailsToLoad = null;
    private String dishIdToLoad = null;
    private double priceToLoad = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recipeTV = view.findViewById(R.id.recipeTV);
        saveRecipeBT = view.findViewById(R.id.saveRecipeBT);
        ingredientsListView = view.findViewById(R.id.ingredientsListView);
        recipeTotalPriceTV = view.findViewById(R.id.recipeTotalPriceTV);
        originalDishTotalPriceTV = view.findViewById(R.id.originalDishTotalPriceTV);

        // Sets up the click listener for the "Save Recipe" button.
        saveRecipeBT.setOnClickListener(v -> {
            if (currentRecipe != null) {
                FirebaseDataHandler.saveRecipe(currentRecipe, result -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dishToLoad != null) {
            recipeTV.setText("loading...");
            saveRecipeBT.setVisibility(View.INVISIBLE);

            loadRecipe(dishToLoad, detailsToLoad, dishIdToLoad, priceToLoad, originRestaurant,getActivity());

            // Clear the fields to prevent reloading on subsequent onResume calls
            dishToLoad = null;
            detailsToLoad = null;
            dishIdToLoad = null;
            priceToLoad = 0.0;
        }
    }

    /**
     * Loads a recipe. It first checks Firestore for an existing recipe for the given dishId.
     * If not found, it generates a new one using the AI.
     * @param dish The name of the dish.
     * @param details Additional details for the AI if generating a new recipe.
     * @param dishId The unique ID of the dish to check against Firestore.
     * @param price The price of the dish.
     * @param originRestaurant the restaurant the dish is from.
     * @param activity The parent activity context.
     */
    public void loadRecipe(String dish, String details, String dishId, double price,String originRestaurant ,Activity activity) {
        if (details == null) {
            details = "";
        }

        currentRecipe = new Recipe(dish,originRestaurant);
        currentRecipe.setOriginDish(dishId);
        currentRecipe.setOriginalPrice(price);
        final String dummyDetails = details;

        new Thread(() -> {
            final Semaphore checkSem = new Semaphore(0);
            final Recipe[] foundRecipe = {null};

            FirebaseDataHandler.checkExistingRecipe(dishId, recipe -> {
                Log.i("myComments", "Found recipe: " + recipe);
                foundRecipe[0] = recipe;
                checkSem.release();
            });

            try {
                checkSem.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("RecipeFragment", "Interrupted while checking existing recipe", e);
            }

            if (foundRecipe[0] != null) {
                currentRecipe = foundRecipe[0];
                updateUiWithRecipe(activity);
            } else {
                getRecipe(AiHandler.AIClient, currentRecipe, dummyDetails);

                try {
                    aiSem.acquire();
                    Ingredients_Handler.updateIngredientWeight(currentRecipe);
                    Ingredients_Handler.getIngredients(currentRecipe.getIngredients().keySet());
                    Ingredients_Handler.ingSem.acquire(2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                updateUiWithRecipe(activity);
            }
        }).start();
    }

    /**
     * Helper method to update the TextView and Button on the main UI thread.
     * @param activity The parent activity context.
     */
    private void updateUiWithRecipe(Activity activity) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                // We add another null check for the view, just in case the fragment
                // was detached while the background thread was running.
                if (getView() != null) {
                    if (currentRecipe.isLoaded()) {
                        recipeTV.setText(currentRecipe.getRecipe());
                        saveRecipeBT.setVisibility(View.VISIBLE);

                        ArrayList<Map.Entry<Ingredient, String>> ingredients = new ArrayList<>(currentRecipe.getIngredients().entrySet());
                        IngredientAdapter adapter = new IngredientAdapter(getContext(), ingredients);
                        ingredientsListView.setAdapter(adapter);

                        double recipeTotalPrice = 0;
                        for (Map.Entry<Ingredient, String> entry : ingredients) {
                            recipeTotalPrice += entry.getKey().getPrice();
                        }
                        recipeTotalPriceTV.setText(String.format("Recipe Total Price: %.2f", recipeTotalPrice));
                        originalDishTotalPriceTV.setText(String.format("Original Dish Total Price: %.2f", currentRecipe.getOriginalPrice()));
                    }
                    else {
                        ingredientsListView.setVisibility(View.INVISIBLE);
                        recipeTotalPriceTV.setText("no price avaible");
                        originalDishTotalPriceTV.setText(String.format("Original Dish Total Price: %.2f", currentRecipe.getOriginalPrice()));
                        recipeTV.setText("recipe not found");
                        saveRecipeBT.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    /**
     * Displays a recipe that was selected from the saved recipes list.
     * Hides the save button since it's already saved.
     * @param recipe The Recipe object to display.
     */
    public void displayRecipe(Recipe recipe) {
        this.currentRecipe = recipe;
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (getView() != null) {
                    Log.i("myComments", recipe.getRecipe());
                    recipeTV.setText(recipe.getRecipe());
                    saveRecipeBT.setVisibility(View.INVISIBLE);

                    ArrayList<Map.Entry<Ingredient, String>> ingredients = new ArrayList<>(recipe.getIngredients().entrySet());
                    IngredientAdapter adapter = new IngredientAdapter(getContext(), ingredients);
                    ingredientsListView.setAdapter(adapter);

                    double recipeTotalPrice = 0;
                    for (Map.Entry<Ingredient, String> entry : ingredients) {
                        recipeTotalPrice += entry.getKey().getPrice();
                    }
                    recipeTotalPriceTV.setText(String.format("Recipe Total Price: %.2f", recipeTotalPrice));
                    originalDishTotalPriceTV.setText(String.format("Original Dish Total Price: %.2f", recipe.getOriginalPrice()));
                }
            });
        }
    }

    /**
     * Constructs the prompt and sends a request to the Gemini AI to generate a recipe.
     * @param aiHandler The GeminiHelper instance.
     * @param recipe The recipe object to be populated.
     * @param details Extra context about the dish for the AI.
     */
    private void getRecipe(GeminiHelper aiHandler, Recipe recipe, final String details) {
        if (recipe != null && !recipe.getName().isEmpty()) {
            String request = "please answer me in the given format for the dish: \"" + recipe.getName() + "\"";

            if (!details.isEmpty()) {
                request += ", here is some extra details about the dish: \"" + details + "\"";
            }
            request += ". if its not a real dish answer with 'this isn't a dish', else answer me with a json format that starts with the 'recipe' : 'recipe steps...', and then individual ingredients with their amount as value (amount will be given as string for example- 'flour' : '50 grams'). do not respond with any other output except from the json itself";
            aiHandler.askGemini(request, r -> {
                Log.i("myComments", "got recipe response:\n" +r);
                prepareRecipe(r, recipe);
                aiSem.release();
            });
        }
    }

    /**
     * Parses the JSON response from the AI and populates the Recipe object.
     * @param response The raw JSON string from the AI.
     * @param recipe The recipe object to populate.
     */
    private void prepareRecipe(String response, Recipe recipe) {
        JSONObject jsonRecipe;
        if (response != null && !response.equals("this isn't a dish")) {
            try {
                response = response.replace("json", "").replace("```", "").trim();
                jsonRecipe = new JSONObject(response);
                java.util.Iterator<String> keys = jsonRecipe.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonRecipe.get(key);

                    if (!key.equals("recipe"))
                        recipe.addIngredient(new Ingredient(key), value.toString());
                    else
                        recipe.setRecipe(value.toString());
                }

                recipe.setLoaded(true);
            } catch (JSONException e) {
                recipe.setRecipe("error getting recipe");
                recipe.setLoaded(false);
            }
        }
    }
}