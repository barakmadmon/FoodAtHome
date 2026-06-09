package com.example.foodathome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class Ingredients_Handler {
    private final static String MARKETS[] = {"https://www.rami-levy.co.il/he","https://www.shufersal.co.il/online"};
    private final static String FREE_ITEMS[] = {"water"};
    private static final Semaphore aiSem = new Semaphore(0);
    public static final Semaphore ingSem = new Semaphore(0);

    /**
     * This method is intended to get ingredients, but its implementation is currently empty.
     * @return An empty string.
     */
    public String getIngredients() {
        SSLContext sslContext;
        URL url;
        HttpsURLConnection connection;
        String output = "";

        return  output;
    }

    /**
     * Updates the weight of each ingredient in a recipe by using an AI service.
     * The AI is prompted to return the weight in grams for solids and volume in ml for liquids.
     * @param recipe The recipe whose ingredients' weights are to be updated.
     */
    public static void updateIngredientWeight(Recipe recipe)
    {
        new Thread(() -> {

            Log.i("myComments","getting weight");
            String request = "please answer me in the given format: each ingridient has a given name and amount, for each ingredient answer me how much it'll weight in grams.if the ingredient is a liquid give me amount in ml. answer me with a json format 'ingridient_name' : 'weight/volume', the 'weight/volume' will only include the number without measure unit (amount will be given as string for example- 'tomato': '3 medium' will become 'tomato' : '400'. or 'water': '400 ml' will become 'water' : '400' ). do not respond with any other output except from the json itself";
            request += " here is the list of items:\n";
            for (Map.Entry<Ingredient, String> ingredient : recipe.getIngredients().entrySet()) {
                if (ingredient.getKey().getId().isEmpty())
                    request += String.format("%s:%s", ingredient.getKey().getName(), ingredient.getValue());
            }

            AiHandler.AIClient.askGemini(request, response -> {
                if (AiHandler.AIClient.ResponseStatus()) {
                    JSONObject jsonIngrdients;

                    try {
                        if (response.isEmpty())
                            throw (new RuntimeException("empty response"));

                        response = response.replace("json", "");
                        response = response.replace("```", "");
                        Log.i("myComments", response);
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
                    }
                }
                aiSem.release();
            });

            try {
                aiSem.acquire();
            } catch (Exception e) {
                Log.i("myComments", e.toString());
            } finally {
                Log.i("myComments", "weight finish");
                ingSem.release();
            }
        }).start();
    }

    /**
     * Retrieves ingredient details from Firebase. For new ingredients, it searches for them using an AI service.
     * @param ingredients A set of ingredients to be fetched or created.
     */
    public static void getIngredients(Set<Ingredient> ingredients)
    {
        new Thread(() -> {
            Log.i("myComments","getting ing");
            Set<Ingredient> newIngredients = new HashSet<>(), completeIngredients = new HashSet<>();

            FirebaseDataHandler.getIngredients(ingredients, false);
            try {
                FirebaseDataHandler.ingredientSem.acquire();
                for (Ingredient ingredient : ingredients) {
                    if ((ingredient.getId() == null || ingredient.getId().isEmpty())) {
                        boolean newI = true;
                        for (String i : FREE_ITEMS) {
                            if (ingredient.getName().equals(i))
                                newI = false;
                        }

                        if (newI) {
                            newIngredients.add(ingredient);
                            Log.i("myComments", ingredient.toString());
                        }
                    } else
                        Log.i("myComments", ingredient.getId());
                }
            } catch (Exception e) {
                Log.i("myComments", e.toString());
            }

            if (!newIngredients.isEmpty()) {
                Log.i("myComments", "new ingredients");
                searchIngredients(newIngredients);
                try { 
                    aiSem.acquire();
                    FirebaseDataHandler.addIngredients(newIngredients);

                    for (Ingredient i : newIngredients) {
                        for (Ingredient j : ingredients) {
                            if (i.getName().equals(j.getName())) {
                                i.copy(j);
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.i("myComments", "failed at get I");
                    Log.i("myComments", e.toString());
                }
            }

            ingSem.release();
            Log.i("myComments", "ingredients got");
        }).start();
    }

    /**
     * Searches for the price and unit weight of a set of ingredients using an AI service.
     * The AI is prompted to use specific websites for price information.
     * @param ingredients A set of ingredients to search for.
     */
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


        format += "for each of these items give me the price of it and the weight per unit in grams, if it is a liquid in ml. answer me in a json format \"price\" : \"unit weight (number only without measure unit)\". ( for example ,for the input 'tomato' output will be \"12.90 : 1000\"). do not respond with any other output except from the json itself.";
        format += "for prices use the websites: ";
        for (String source : MARKETS) {
            format += '\"' + source + "\", ";
        }

        AiHandler.AIClient.askGemini(format, response -> {
            if (AiHandler.AIClient.ResponseStatus()) {

                if ( !response.startsWith("error:") ) {
                    JSONObject jsonIngrdients;
                    response = response.replace("json", "");
                    response = response.replace("```", "");
                    try {
                        jsonIngrdients = new JSONObject(response);
                    } catch (JSONException e) {
                        aiSem.release();
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
                            aiSem.release();
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
                        } while (free);

                        ingredient.setPrice(Float.parseFloat(parts[0]));
                        ingredient.setUnitSize(Float.parseFloat(parts[1]));
                    }
                }
            }
            aiSem.release();
        });
    }
}