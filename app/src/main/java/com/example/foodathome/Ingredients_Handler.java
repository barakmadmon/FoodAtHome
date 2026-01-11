package com.example.foodathome;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class Ingredients_Handler {
    final static String MARKETS[] = {"https://www.rami-levy.co.il/he","https://www.shufersal.co.il/online"};
    final static String FREE_ITEMS[] = {"water"};
    static final Semaphore aiSem = new Semaphore(0);
    static final Semaphore ingSem = new Semaphore(0);
    private static final Logger log = LoggerFactory.getLogger(Ingredients_Handler.class);

    public String getIngredients() {
        SSLContext sslContext;
        URL url;
        HttpsURLConnection connection;
        String output = "";

        return  output;
    }

    public static void updateIngredientWeight(Recipe recipe)
    {
        //Handler mainHandler = new Handler(Looper.getMainLooper());
        String request = "please answer me in the given format: each ingridient has a given name and amount, for each ingredient answer me how much it'll weight in grams.if the ingredient is a liquid give me amount in ml.answer me with a json format 'ingridient_name' : 'weight in grams' (amount will be given as string for example- 'tomato': '3 medium' will become 'tomato' : '400'). do not respond with any other output except from the json itself";
        request += " here is the list of items:\n";
        for(Map.Entry<Ingredient, String> ingredient : recipe.getIngredients().entrySet()) {
            if(ingredient.getKey().getId().isEmpty())
                request += String.format("%s:%s",ingredient.getKey().getName(),ingredient.getValue());
        }


        Log.i("myComments", request);
        AiHandler.AIClient.askGemini(request, response -> {
            JSONObject jsonIngrdients;
            response = response.replace("json", "");
            response = response.replace("```", "");
            Log.i("myComments", response);
            try {
                jsonIngrdients = new JSONObject(response);
                java.util.Iterator<String> keys = jsonIngrdients.keys();
                Iterator<Ingredient> iterator = recipe.getIngredients().keySet().iterator();

                while (keys.hasNext() && iterator.hasNext()) {
                    String key = keys.next();

                    Ingredient ingredient = iterator.next();
                    ingredient.setAmount(Float.parseFloat(jsonIngrdients.get(key).toString()));
                }

            } catch (JSONException e) {
                Log.i("myComments", e.toString());
            } finally {
                aiSem.release();
            }
        });

        try {
            aiSem.acquire();
        }
        catch (Exception e){
            Log.i("myComments", e.toString());
        }
        finally {
            ingSem.release();
        }
    }

    public static void getIngredients(Set<Ingredient> ingredients)
    {
        Set<Ingredient> newIngredients = new HashSet<>(), completeIngredients = new HashSet<>();

        FirebaseDataHandler.getIngredients(ingredients,false);
        try {
            FirebaseDataHandler.ingredientSem.acquire();
            for (Ingredient ingredient : ingredients) {
                if (ingredient.getId() == null)
                    newIngredients.add(ingredient);
            }
        } catch (Exception e) {
            Log.i("myComments", e.toString());
        }

        if(!newIngredients.isEmpty()) {
            searchIngredients(newIngredients);
            try { // no finally becuase ingSem is also released when there no new ingredients
                aiSem.acquire();
                FirebaseDataHandler.addIngredients(newIngredients);
                for(Ingredient ingredient: ingredients) {
                    if (ingredient.getId() != null)
                        newIngredients.add(ingredient);
                }
            } catch (Exception e) {
                Log.i("myComments", e.toString());
            }
        }

        ingSem.release();


    }

    private static void searchIngredients(Set<Ingredient> ingredients) {

        String format = "i have the next list:";
        for (Ingredient ingredient : ingredients) {
            if(ingredient.getId() != null)
                format += ingredient.getName() +", ";
        }

        for(String item: FREE_ITEMS)
        {
            format = format.replace(", "+item + ", ",", ");
        }


        format += "for each of these items give me the price of it and the weight per unit in grams, if it is a liquid in ml. answer me in a json format \"price\" : \"unit weight\". ( for example ,for the input 'tomato' output will be \"12.90 : 1000\"). do not respond with any other output except from the json itself.";
        format += "for prices use the websites: ";
        for (String source : MARKETS) {
            format += '\"' + source + "\", ";
        }

        AiHandler.AIClient.askGemini(format, response -> {
            JSONObject jsonIngrdients;
            response = response.replace("json", "");
            response = response.replace("```", "");
            Log.i("myComments", response);
            try {
                jsonIngrdients = new JSONObject(response);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            java.util.Iterator<String> keys = jsonIngrdients.keys();
            Iterator<Ingredient> iterator = ingredients.iterator();

            while (keys.hasNext() && iterator.hasNext()) {
                String key = keys.next();
                String valueString = null;


                try {
                    valueString = jsonIngrdients.getString(key);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String[] parts = valueString.split(" : ");
                Ingredient ingredient;
                boolean free = false;
                do {
                    ingredient = iterator.next();
                    free = false;
                    for (String item : FREE_ITEMS) {
                        if (ingredient.getName().contains(item)) {
                            ingredient.setPrice(0);
                            free = true;
                            break;
                        }
                    }
                }while(free);

                ingredient.setPrice(Float.parseFloat(parts[0]));
                ingredient.setUnitSize(Float.parseFloat(parts[1]));
            }
            aiSem.release();
        });
    }
}
