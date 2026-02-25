package com.example.foodathome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Semaphore;

public class RestaurantHelper {
    private static final Semaphore aiSem = new Semaphore(0);

    static public void searchDishes(Restaurant restaurant) {
        //FirebaseDataHandler.getRestaurant(restaurant.getName()); isnt yet implemented in this stage

        if (restaurant.getDishes().isEmpty()) {
            String format = "give me a json object, that contains a list of items from the source: \"" + restaurant.getWebsite() + "\"\n";
            format += "the format for each object should look like:\n' \"dish_name\": \"website_description\", \"dish_name\": \"website_description\",...'\n";
            format += "if dish dont have discription \"website_description\" should be \"None\". respond with only the json object, if you cant find dishes, you give the response \"this isn't a restaurant\".";

            AiHandler.AIClient.askGemini(format, response -> {
            /*String  response = "{"
                        +"\"Family Pizza\": \"A customizable pizza where you can choose your favorite type of dough and variety of toppings.\","
                        +"\"Personal Pizza\": \"A personal-sized pizza that opens from the classic Domino's dough ball.\","
                        +"\"Cheesy Crust\": \"Our signature pizza with edges stuffed full of quality mozzarella cheese.\","
                        +"\"Carnivore MIX\": \"A meat-heavy pizza featuring pepperoni, cabanos, beef brisket, and spicy chipotle sauce.\","
                        +"\"Mozzarella Fingers\": \"Fresh dough fingers topped with 100% mozzarella cheese, served with garlic dip.\","
                        +"\"Garlic Bread\": \"Seasoned with garlic butter and herbs, Grana Padano parmesan, and mozzarella.\","
                        +"\"Gluten-Free Pizza\": \"A pizza made with gluten-free ingredients, though baked in a common kitchen environment.\","
                        +"\"Volcano Cheddar Pizza\": \"A specialty pizza featuring a bubbly, melted cheddar crust.\","
                        +"\"Meat Rolls\": \"8 units of delicious rolls filled with garlic butter and slices of premium meats.\","
                        +"\"Cheesy Olive Rolls\": \"8 hot rolls filled with mozzarella, pizza sauce, and green olives.\","
                        +"\"Vegan Super Sandwich\": \"A crunchy ciabatta filled with pesto, vegan cheese, and fresh mushrooms.\","
                        +"\"Potatoes\": \"Seasoned and baked potato wedges, served with a choice of dipping sauce.\","
                        +"\"Cacho a Pepe Pizza\": \"A gourmet pizza featuring a cream-based Cacho a Pepe sauce and mushrooms.\","
                        +"\"Greek MIX\": \"A Mediterranean-inspired pizza topped with diced tomatoes, red onion, Kalamata olives, and Bulgarian cheese.\","
                        +"\"Israeli MIX\": \"A local favorite featuring mushrooms, onions, and green olives.\","
                        +"\"Chocolate Fondant\": \"A rich, hot chocolate cake with a molten center.\","
                        +"\"Cinnamon Bites\": \"Warm cinnamon-dusted pastry bites served with a salted caramel dipping sauce.\","
                        +"\"White Chocolate and Pistachio Pops\": \"Warm pastry pops filled with white chocolate and pistachio cream.\","
                        +"\"Mini Pancakes\": \"A serving of 10 fluffy mini pancakes, perfect for dessert.\" }";// for testing in case use of gemini exceeds quota work*/
                prepareDishes(restaurant, response);
                aiSem.release();
            });
        }
    }

    static public void prepareDishes(Restaurant restaurant,String response) {
        JSONObject jsonRecipe;

        if(response != null && !response.isEmpty() && !response.equals("this isn't a restaurant")) {
            try {
                response = response.replace("json","");
                response = response.replace("```","");

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
