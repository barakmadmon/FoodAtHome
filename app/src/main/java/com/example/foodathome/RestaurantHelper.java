package com.example.foodathome;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RestaurantHelper {
    private static GeminiHelper aiHandler;

    static public void searchDishes(Restaurant restaurant) {
        String format = "give me a json object, that contains a list of items from the source: \"" + restaurant.getWebsite() + "\"\n";
        format += "the format for each object should look like:\n' \"dish_name\": \"website_description\", \"dish_name\": \"website_description\",...'\n";
        format += "if dish dont have discription \"website_description\" should be \"None\". if you cant find dishes, you give the response \"this isn't a restaurant\"";
        aiHandler.askGemini(format, response -> {
            prepareDishes(restaurant,response);
        });
    }

    static private void prepareDishes(Restaurant restaurant,String response) {
        JSONObject jsonRecipe;

        if(!response.equals("this isn't a restaurant")) {
            try {
                response = response.replace("json","");
                response = response.replace("```","");
                jsonRecipe = new JSONObject(response);
                java.util.Iterator<String> keys = jsonRecipe.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonRecipe.get(key);



                }
            }
            catch (JSONException e) {
                restaurant;
            }
        }
    }
}
