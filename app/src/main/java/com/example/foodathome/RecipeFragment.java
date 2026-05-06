package com.example.foodathome;

import static android.content.Intent.getIntent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Semaphore;

/*
object for recipe fragment ui

METHODS:
    + onCreateView - initialise
    + loadRecipe - load recipe from database, if not exists gets it from ai, and then update text view with the recipe
        input: dish - name of dish
               details- extra details about dish if there are
               activity - current activity
    - getRecipe - get recipe from ai and update it in recipe
        input: ai handler,
               recipe - object to contain output from ai, need to contain name
               details- extra details about dish if there are
    - prepareRecipe - format ai output and updates recipe object
        input: response - response from ai
               recipe - recipe object to update
*/

public class RecipeFragment extends Fragment {
    private Button saveRecipeBT; // currently isnt called for firebase design sakes
    private TextView recipeTV;
    static final Semaphore aiSem = new Semaphore(0);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        recipeTV = view.findViewById(R.id.recipeTV);
        saveRecipeBT = view.findViewById(R.id.saveRecipeBT);

        String dish = activity.getIntent().getStringExtra("DISH");
        String details = activity.getIntent().getStringExtra("DETAILS");

        if (recipeTV != null) {
            if (dish != null && !dish.isEmpty()) {
                recipeTV.setText("loading...");
                loadRecipe(dish, details, activity);

            } else {
                recipeTV.setText("recipe not found");
                saveRecipeBT.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void loadRecipe(String dish,String details, Activity activity) {
        if (details == null) {
            details = "";
        }

        Recipe recipe = new Recipe(dish);
        Log.i("myComments", dish);

        final String dummyDetails = details; // because this is dum dum
        new Thread(() -> {
            /*getRecipe(AiHandler.AIClient, recipe, new String(dummyDetails));

            try {
                aiSem.acquire();   // waits for signal
                Log.i("myComments", "weight release");
                Ingredients_Handler.updateIngredientWeight(recipe);
                // waits for signal
                Ingredients_Handler.getIngredients(recipe.getIngredients().keySet());
                Ingredients_Handler.ingSem.acquire(2);

                Log.i("myComments", "ui release");// waits for signal
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/

            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("myComments", "posting recipe");
                        recipeTV.setText(recipe.toString());
                        saveRecipeBT.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();

    }

    private void getRecipe(GeminiHelper aiHandler,Recipe recipe,final String details){
        if(recipe != null && !recipe.getName().isEmpty()) {
            //"chicken soup", "chicken soup made with love not chicken and spring onions"
            String request = "please answer me in the given format for the dish: \"" +recipe.getName()+"\"";

            if(!details.isEmpty()) {
                request += ", here is some extra details about the dish: \"" + details+"\"";
            }
            request += ". if its not a real dish answer with 'this isn't a dish', else answer me with a json format that starts with the 'recipe' : 'recipe steps...', and then individual ingredients with their amount as value (amount will be given as string for example- 'flour' : '50 grams'). do not respond with any other output except from the json itself";
            Log.i("myComments", request);
            aiHandler.askGemini(request, r -> {
                Log.i("myComments", r);
                prepareRecipe(r, recipe);
                aiSem.release();
            });
        }

    }

    private void prepareRecipe(String response, Recipe recipe) {
        JSONObject jsonRecipe;

        if(!response.equals("this isn't a dish")) {
            try {
                response = response.replace("json","");
                response = response.replace("```","");
                jsonRecipe = new JSONObject(response);
                java.util.Iterator<String> keys = jsonRecipe.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonRecipe.get(key);

                    if(!key.equals("recipe"))
                        recipe.addIngredient(new Ingredient(key), value.toString());
                    else
                        recipe.setRecipe(value.toString());
                }
            }
            catch (JSONException e) {
                recipe.setRecipe("error getting recipe");
            }
        }
    }
}
