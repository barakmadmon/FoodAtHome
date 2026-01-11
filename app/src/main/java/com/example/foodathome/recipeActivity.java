package com.example.foodathome;

import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
//import com.google.firebase.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

public class recipeActivity extends AppCompatActivity{
    TextView recipeTV;
    static final Semaphore aiSem = new Semaphore(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String dish = getIntent().getStringExtra("DISH");
        Recipe recipe = new Recipe(dish);
        Log.i("myComments", dish);
        recipeTV = findViewById(R.id.recipeTV);

        new Thread(() -> {
            getRecipe(AiHandler.AIClient,recipe,"");

            try {
                aiSem.acquire();   // waits for signal
                for (Ingredient ingredient : recipe.getIngredients().keySet()) {
                    Log.i("myComments", ingredient.toString());
                }
                Log.i("myComments", "weight release");
                Ingredients_Handler.updateIngredientWeight(recipe);
                Ingredients_Handler.ingSem.acquire();  // waits for signal
                Log.i("myComments", "price release");
                Ingredients_Handler.getIngredients(recipe.getIngredients().keySet());
                Ingredients_Handler.ingSem.acquire();

                Log.i("myComments", "ui release");// waits for signal
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("myComments", "posting recipe");
                    recipeTV.setText(recipe.toString());
                }

            });
        }).start();
    }

    public void getRecipe(GeminiHelper aiHandler,Recipe recipe,String details){
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
                recipe.setRecipe("json error");
            }
        }
    }

}