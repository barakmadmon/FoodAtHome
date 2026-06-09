package com.example.foodathome;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Semaphore;

/**
 * Helper class for handling restaurant data.
 */
public class RestaurantHelper {
    private static final Semaphore aiSem = new Semaphore(0);
    private static final Semaphore searchSem = new Semaphore(0);

    /**
     * Searches for dishes of a restaurant using AI if they are not already present.
     * @param restaurant The restaurant to search dishes for.
     */
    private static void searchDishes(Restaurant restaurant) {
        Log.i("myComments", restaurant.getWebsite());
        if (restaurant.getDishes().isEmpty()) {
            String format = "give me a json object, that contains the restaurant name and a list of items from the source: \"" + restaurant.getWebsite() + "\" and other sub domains\n";
            format += "the format should look like:\n";
            format += "{\n  \"name\": \"Restaurant Name\",\n  \"dishes\": [[\"dish_name\", price (only number) ,\"website_description\"], [\"dish_name\", price ,\"website_description\"],...]\n}\n";
            format += "convert the price of the dishes to israel new shekel if it is in another currency";
            format += "if dish dont have discription \"website_description\" should be \"None\". respond with only the json object, if you cant find dishes, you give the response \"could not find restaurant\".";

            AiHandler.AIClient.askGemini(format, response -> {
                Log.i("myComments", response);
                if (AiHandler.AIClient.ResponseStatus()) {
                    prepareDishes(restaurant, response);

                    if (!restaurant.getDishes().isEmpty()) {
                        FirebaseDataHandler.addRestaurant(restaurant, result -> {
                            Log.i("myComments", "AI results saved to Firebase: " + result);
                        });
                    }
                }
                aiSem.release();
            });
        } else {
            aiSem.release();
        }
    }

    /**
     * Parses the AI response and prepares the dishes for the restaurant.
     * @param restaurant The restaurant to add the dishes to.
     * @param response The AI response containing the dish information.
     */
    private static void prepareDishes(Restaurant restaurant, String response) {
        if (response != null && !response.isEmpty() && !response.equals("this isn't a restaurant") && !response.contains("could not find restaurant")) {
            try {
                response = response.replace("json", "");
                response = response.replace("```", "").trim();

                Log.i("myComments", "Parsing dishes response: " + response);

                if (response.startsWith("[")) {
                    parseDishArray(restaurant, new JSONArray(response));
                } else if (response.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.has("name") && (restaurant.getName() == null || restaurant.getName().isEmpty() || restaurant.getName().equals("Not found"))) {
                        restaurant.setName(jsonObject.getString("name"));
                    }

                    if (jsonObject.has("dishes")) {
                        parseDishArray(restaurant, jsonObject.getJSONArray("dishes"));
                    } else {
                        java.util.Iterator<String> keys = jsonObject.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (!key.equals("name")) {
                                Object value = jsonObject.get(key);
                                if (value instanceof JSONArray) {
                                    parseDishArray(restaurant, (JSONArray) value);
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("myComments", "Error parsing restaurant dishes: " + e.getMessage());
                restaurant.getDishes().clear();
            }
        }
    }

    /**
     * Parses a JSON array of dishes and adds them to the restaurant.
     * @param restaurant The restaurant to add the dishes to.
     * @param array The JSON array of dishes.
     * @throws JSONException If there is an error parsing the JSON.
     */
    private static void parseDishArray(Restaurant restaurant, JSONArray array) throws JSONException {
        Log.i("myComments", "Parsing dish array: " + array.toString());

        for (int i = 0; i < array.length(); i++) {
            Object item = array.get(i);
            if (item instanceof JSONArray) {
                JSONArray dishInfo = (JSONArray) item;
                if (dishInfo.length() >= 3) {
                    String name = dishInfo.getString(0);
                    double price = dishInfo.optDouble(1, 0.0);
                    String details = dishInfo.getString(2);

                    RestaurantDish dish = new RestaurantDish(name, details, restaurant.getId(), price);
                    restaurant.addDish(dish);
                }
            }
        }
    }

    /**
     * Gets the dishes for a restaurant, either from Firebase or by searching with AI.
     * @param restaurant The restaurant to get the dishes for.
     */
    public static void getRestaurantDishes(Restaurant restaurant) {
        final LatLng initialLocation = restaurant.getLocation();
        restaurant.getDishes().clear();

        FirebaseDataHandler.searchRestaurantByWebsite(restaurant.getWebsite(), restaurant, result -> {
            boolean shouldRunAi = false;

            if (!"Success".equals(result)) {
                // Not found in Firebase
                Log.i("myComments", "Firebase doesn't have restaurant: " + result);
                if (initialLocation != null) {
                    Log.i("myComments", "Using initial location");
                    restaurant.setLocation(initialLocation);
                }
                shouldRunAi = true;
            } else {
                // Found in Firebase
                Log.i("myComments", "Found restaurant in Firebase");
                if (initialLocation != null) {
                    restaurant.setLocation(initialLocation);
                    FirebaseDataHandler.updateRestaurantLocation(restaurant);
                }

                if (restaurant.getDishes().isEmpty()) {
                    Log.i("myComments", "Firebase menu is empty, using AI");
                    shouldRunAi = true;
                }
            }

            if (shouldRunAi) {
                try {
                    searchDishes(restaurant);
                    Log.i("myComments", "AI searching");
                    aiSem.acquire();
                } catch (InterruptedException e) {
                    Log.e("myComments", "AI search interrupted: " + e.getMessage());
                }
            }

            restaurant.setLoaded();
            Log.i("myComments", "Releasing search semaphore");
            searchSem.release();
        });
    }

    /**
     * Searches for a restaurant by its website.
     * @param restaurant The restaurant object to populate with data.
     * @param website The website of the restaurant to search for.
     */
    public static void searchResteraunt(Restaurant restaurant, String website) {
        Log.i("myComments", "website: " + website);
        try {
            restaurant.setWebsite(website);
            getRestaurantDishes(restaurant);
            searchSem.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}