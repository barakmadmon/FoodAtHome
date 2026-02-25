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

public class RecipeFragment extends Fragment {
    private Button saveRecipeBT; // currently isnt called for firebase design sakes
    private TextView recipeTV;
    static final Semaphore aiSem = new Semaphore(0);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);
        Activity activity = getActivity();
        recipeTV = view.findViewById(R.id.recipeTV);

        String dish = activity.getIntent().getStringExtra("DISH");
        String details = activity.getIntent().getStringExtra("DETAILS");

        if (recipeTV != null)
            if ( dish != null && !dish.isEmpty())
                loadRecipe(dish,details,activity);
            else {
                recipeTV.setText("recipe not found");
            }


        return view;
    }

    public void loadRecipe(String dish,String details, Activity activity) {
        if (details == null) {
            details = "";
        }

        Recipe recipe = new Recipe(dish);
        Log.i("myComments", dish);

        final String dummyDetails = details; // because this is dum dum
        new Thread(() -> {
            getRecipe(AiHandler.AIClient, recipe, new String(dummyDetails));

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
            }

            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("myComments", "posting recipe");
                        recipeTV.setText(recipe.toString());
                    }
                });
            }
        }).start();

    }

    public void getRecipe(GeminiHelper aiHandler,Recipe recipe,final String details){
        if(recipe != null && !recipe.getName().isEmpty()) {
            //"chicken soup", "chicken soup made with love bacon and spring onions"
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

    public void prepareRecipe(String response, Recipe recipe) {
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
