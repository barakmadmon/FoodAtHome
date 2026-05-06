package com.example.foodathome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Semaphore;

/* METHODS:
    - searchDishes - load restaurant data from database (will be implemented in code soon), if not exists search dishes using ai
        input: restaurant - object to load data to
    - prepareDishes - format ai output and updates restaurant object
        input: response - response from ai
               restaurant - restaurant object to update
    + getRestaurantDishes - get restaurants dishes,
        input: restaurant - object to load data to
*/

public class RestaurantHelper {
    private static final Semaphore aiSem = new Semaphore(0);

    static private void searchDishes(Restaurant restaurant) {

        if (restaurant.getDishes().isEmpty()) {
            String format = "give me a json object, that contains a list of items from the source: \"" + restaurant.getWebsite() + "\" located at\n";
            format += "the format for each object should look like:\n' \"dish_name\": \"website_description\", \"dish_name\": \"website_description\",...'\n";
            format += "if dish dont have discription \"website_description\" should be \"None\". respond with only the json object, if you cant find dishes, you give the response \"could not find restaurant\".";

            AiHandler.AIClient.askGemini(format, response -> {
                prepareDishes(restaurant, response);
                aiSem.release();
                Log.i("myComments", response);
            });
        }
    }

    static private void prepareDishes(Restaurant restaurant,String response) {
        JSONObject jsonRecipe;

        if(response != null && !response.isEmpty() && !response.equals("this isn't a restaurant")) {
            try {
                response = response.replace("json","");
                response = response.replace("```","");

                Log.i("myComments", response);
                jsonRecipe = new JSONObject(response);
                java.util.Iterator<String> keys = jsonRecipe.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonRecipe.get(key);
                    RestaurantDish dish = new RestaurantDish(key,value.toString(),restaurant.getId());
                    restaurant.addDish(dish);
                }
            }
            catch (JSONException e) {
                restaurant.getDishes().clear();
            }
        }
    }

    static public void getRestaurantDishes(Restaurant restaurant) {
        restaurant.getDishes().clear();
        try {
            searchDishes(restaurant);
            aiSem.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        restaurant.setLoaded();
    }
}
