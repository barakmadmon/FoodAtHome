package com.example.foodathome;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles all interactions with Firebase Firestore, including user authentication,
 * recipe management, and restaurant data retrieval.
 */
public class FirebaseDataHandler {
    static final int BATCH_SIZE = 10;
    private static final String INGREDIENT_COLLECTION = "Ingredients";
    private static final String RECIPE_COLLECTION = "Recipes";
    private static final String RESTAURANT_COLLECTION = "Restaurants";
    private static final String RESTAURANT_DISHES_COLLECTION = "Restaurant_Dishes";
    private static final String USER_COLLECTION = "Users";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static final Semaphore ingredientSem = new Semaphore(0);
    static final Semaphore recipeSem = new Semaphore(0);

    private static User currentUser;
    private static String currentUserDocId;

    /**
     * Returns the currently logged-in user.
     * @return The current User object.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Logs out the current user.
     */
    public static void signoutUser() {
        currentUser = null;
        currentUserDocId = null;
    }

    /**
     * Authenticates a user based on username and password.
     * @param username The name of the user.
     * @param password The user's password.
     * @param callback Callback to return authentication status.
     */
    public static void loginUser(String username, String password, Callback callback) {
        db.collection(USER_COLLECTION)
                .whereEqualTo("name", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        User user = userDoc.toObject(User.class);
                        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
                            currentUser = user;
                            currentUserDocId = userDoc.getId();
                            callback.onDone("Success");
                        } else {
                            callback.onDone("Invalid username or password");
                        }
                    } else {
                        callback.onDone("Invalid username or password");
                    }
                })
                .addOnFailureListener(e -> callback.onDone("Error: " + e.getMessage()));
    }

    /**
     * Registers a new user in the system.
     * @param username Chosen username.
     * @param password Chosen password.
     * @param callback Callback to return signup status.
     */
    public static void signupUser(String username, String password, Callback callback) {
        db.collection(USER_COLLECTION)
                .whereEqualTo("name", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        User newUser = new User(username, password);
                        newUser.setRecipeIds(new ArrayList<>());
                        db.collection(USER_COLLECTION)
                                .add(newUser)
                                .addOnSuccessListener(documentReference -> callback.onDone("Success"))
                                .addOnFailureListener(e -> callback.onDone("Error: " + e.getMessage()));
                    } else {
                        callback.onDone("Username already exists");
                    }
                })
                .addOnFailureListener(e -> callback.onDone("Error: " + e.getMessage()));
    }

    /**
     * Saves a recipe to the global collection (if it doesn't exist) and links it to the current user.
     * @param recipe The Recipe object to save.
     * @param callback Callback to return operation status.
     */
    public static void saveRecipe(Recipe recipe, Callback callback) {
        if (currentUserDocId == null) {
            callback.onDone("User not logged in");
            return;
        }

        Query query;
        if (recipe.getOriginDish() != null && !recipe.getOriginDish().isEmpty()) {
            query = db.collection(RECIPE_COLLECTION).whereEqualTo("originDish", recipe.getOriginDish());
        } else {
            query = db.collection(RECIPE_COLLECTION).whereEqualTo("Name", recipe.getName());
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Map<String, Object> recipeData = new HashMap<>();
                recipeData.put("Name", recipe.getName());
                recipeData.put("Recipe", recipe.getRecipe());
                recipeData.put("originRestaurant", recipe.getOriginRestaurant());
                recipeData.put("originDish", recipe.getOriginDish());

                // **FIX:** Save ingredients as a list of maps, which is supported by Firestore.
                List<Map<String, Object>> ingredientList = new ArrayList<>();
                for (Map.Entry<Ingredient, String> entry : recipe.getIngredients().entrySet()) {
                    Map<String, Object> ingData = new HashMap<>();
                    ingData.put("Grams", entry.getKey().getAmount());
                    ingData.put("IngredientID", entry.getKey().getId());
                    ingData.put("IngredientAmount", entry.getValue());
                    ingredientList.add(ingData);
                }
                recipeData.put("Ingredients", ingredientList);

                db.collection(RECIPE_COLLECTION).add(recipeData)
                        .addOnSuccessListener(docRef -> {
                            recipe.setId(docRef.getId());
                            addRecipeToUser(docRef.getId(), callback);
                        })
                        .addOnFailureListener(e -> callback.onDone("Error saving recipe: " + e.getMessage()));
            } else {
                String existingId = queryDocumentSnapshots.getDocuments().get(0).getId();
                recipe.setId(existingId);
                addRecipeToUser(existingId, callback);
            }
        });
    }

    /**
     * Internal helper to add a global recipe ID to a specific user's favorites list.
     */
    private static void addRecipeToUser(String recipeId, Callback callback) {
        db.collection(USER_COLLECTION).document(currentUserDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> recipeIds = (List<String>) doc.get("recipeIds");
                    if (recipeIds == null) recipeIds = new ArrayList<>();
                    
                    if (!recipeIds.contains(recipeId)) {
                        recipeIds.add(recipeId);
                        db.collection(USER_COLLECTION).document(currentUserDocId)
                                .update("recipeIds", recipeIds)
                                .addOnSuccessListener(v -> callback.onDone("Success"))
                                .addOnFailureListener(e -> callback.onDone("Error updating user: " + e.getMessage()));
                    } else {
                        callback.onDone("Recipe already saved");
                    }
                });
    }

    /**
     * Removes a recipe from the current user's favorite list.
     * @param recipeId The document ID of the recipe to remove.
     * @param callback Callback to return status.
     */
    public static void removeRecipeFromUser(String recipeId, Callback callback) {
        if (currentUserDocId == null) {
            callback.onDone("User not logged in");
            return;
        }

        db.collection(USER_COLLECTION).document(currentUserDocId)
                .update("recipeIds", FieldValue.arrayRemove(recipeId))
                .addOnSuccessListener(aVoid -> callback.onDone("Success"))
                .addOnFailureListener(e -> callback.onDone("Error removing recipe: " + e.getMessage()));
    }

    /**
     * Fetches all recipes currently in the user's favorite list.
     * @param recipesList The list to populate with fetched Recipe objects.
     * @param callback Callback returned after the list is populated.
     */
    public static void getUserRecipes(List<Recipe> recipesList, Callback callback) {
        if (currentUserDocId == null) {
            callback.onDone("User not logged in");
            return;
        }

        db.collection(USER_COLLECTION).document(currentUserDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<String> recipeIds = (List<String>) doc.get("recipeIds");
                    if (recipeIds == null || recipeIds.isEmpty()) {
                        recipesList.clear();
                        callback.onDone("Success");
                        return;
                    }

                    List<String> validIds = new ArrayList<>();
                    for (String id : recipeIds) {
                        if (id != null && !id.isEmpty()) {
                            validIds.add(id);
                        }
                    }

                    if (validIds.isEmpty()) {
                        recipesList.clear();
                        callback.onDone("Success");
                        return;
                    }

                    db.collection(RECIPE_COLLECTION)
                            .whereIn(FieldPath.documentId(), validIds)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                recipesList.clear();
                                for (DocumentSnapshot recipeDoc : querySnapshot.getDocuments()) {
                                    recipesList.add(getRecipe(recipeDoc));
                                }
                                callback.onDone("Success");
                            })
                            .addOnFailureListener(e -> callback.onDone("Error fetching recipes: " + e.getMessage()));
                });
    }

    /**
     * Adds a set of ingredients to the global ingredients list if they don't already exist.
     * @param ingredients The set of Ingredients to sync with Firestore.
     */
    public static void addIngredients(Set<Ingredient> ingredients) {
        Set<String> existingNames = new HashSet<>();
        AtomicInteger completed = new AtomicInteger(0);
        ArrayList<String> names =  new ArrayList<>();
        Map<Ingredient, DocumentReference> newIngredientRefs = new HashMap<>();

        for (Ingredient ing : ingredients) {
            names.add(ing.getName());
        }

        if (names.isEmpty()) return;

        for (int i = 0; i < names.size(); i += BATCH_SIZE) {
            List<String>nameChunk = names.subList(i, Math.min(names.size(), i + BATCH_SIZE));

            db.collection(INGREDIENT_COLLECTION)
                    .whereIn("name",nameChunk)
                    .get()
                    .addOnSuccessListener((QuerySnapshot qs) -> {
                        for (DocumentSnapshot doc : qs.getDocuments()) {
                            Ingredient ing = doc.toObject(Ingredient.class);
                            if (ing != null)
                                existingNames.add(ing.getName());
                        }

                        if (completed.incrementAndGet() == Math.ceil((double) names.size() / BATCH_SIZE)) {
                            WriteBatch batch = db.batch();

                            for (Ingredient ing : ingredients) {
                                if (!(existingNames.contains(ing.getName()))) {
                                    DocumentReference newDoc = db.collection(INGREDIENT_COLLECTION).document();
                                    batch.set(newDoc, ing);
                                    newIngredientRefs.put(ing, newDoc);
                                }
                            }
                            batch.commit().addOnSuccessListener(v -> {
                                for (Map.Entry<Ingredient, DocumentReference> entry : newIngredientRefs.entrySet()) {
                                    entry.getKey().setId(entry.getValue().getId());
                                }
                            });
                        }
                    });
        }
    }

    /**
     * Resolves ingredient data from Firestore based on ID or Name.
     * @param ingredients The set to be resolved.
     * @param recipe True if matching by document ID, false if matching by name.
     */
    public static void getIngredients(Set<Ingredient> ingredients, boolean recipe) {
        AtomicInteger completed = new AtomicInteger(0);
        ArrayList<String> ingrdientIDS;
        Map<String, Ingredient> ingredientMap = new HashMap<>();

        for (Ingredient ing : ingredients) {
            if(recipe)
                ingredientMap.put(ing.getId(), ing);
            else
                ingredientMap.put(ing.getName(), ing);
        }

        ingrdientIDS = new ArrayList<>();
        for (String id : ingredientMap.keySet()) {
            if (id != null && !id.isEmpty()) {
                ingrdientIDS.add(id);
            }
        }

        if (ingrdientIDS.size() == 0) {
            onAllIngredientsResolved(ingredients);
            return;
        }

        for (int i = 0; i < ingrdientIDS.size(); i += BATCH_SIZE) {
            List<String> idChunk = ingrdientIDS.subList(i, Math.min(ingrdientIDS.size(), i + BATCH_SIZE));

            Query q;
            if(recipe) {
                q =  db.collection(INGREDIENT_COLLECTION).whereIn(FieldPath.documentId(), idChunk);
            }
            else {
                q =  db.collection(INGREDIENT_COLLECTION).whereIn("name", idChunk);
            }

            q.get().addOnSuccessListener((QuerySnapshot qs) -> {
                for (DocumentSnapshot doc : qs.getDocuments()) {
                    Ingredient temp = doc.toObject(Ingredient.class);
                    if (temp != null) {
                        temp.setId(doc.getId());
                        if(recipe) {
                            ingredientMap.get(temp.getId()).copy(temp);
                        }
                        else {
                            ingredientMap.get(temp.getName()).copy(temp);
                        }
                    }
                }

                if (completed.incrementAndGet() == Math.ceil((double) ingrdientIDS.size() / BATCH_SIZE)) {
                    onAllIngredientsResolved(ingredients);
                }
            });
        }
    }

    /**
     * Internal handler called when all ingredient queries in a batch are complete.
     */
    private static void onAllIngredientsResolved(Set<Ingredient> myIngredients) {
        ingredientSem.release();
    }

    /**
     * Converts a Firestore DocumentSnapshot into a Recipe object.
     * @param doc The Firestore document snapshot.
     * @return A fully populated Recipe object.
     */
    public static Recipe getRecipe(DocumentSnapshot doc) {
        String name = doc.getString("Name");
        String restaurantName = doc.getString("originRestaurant");
        if (name == null) name = "Unknown Recipe";
        Recipe recipe = new Recipe(name,restaurantName);
        recipe.setId(doc.getId());
        recipe.setRecipe(doc.getString("Recipe"));
        recipe.setOriginRestaurant(doc.getString("originRestaurant"));
        recipe.setOriginDish(doc.getString("originDish"));

        // **FIX:** Parse the ingredients field as a List of Maps.
        Object ingredientsObj = doc.get("Ingredients");
        if (ingredientsObj instanceof List) {
            List<?> ingredientsList = (List<?>) ingredientsObj;
            for (Object item : ingredientsList) {
                if (item instanceof Map) {
                    Map<String, Object> ingData = (Map<String, Object>) item;
                    Ingredient temp = new Ingredient("");

                    Object idObj = ingData.get("IngredientID");
                    if (idObj instanceof String) {
                        temp.setId((String) idObj);
                    }

                    Object gramsObj = ingData.get("Grams");
                    if (gramsObj instanceof Number) {
                        temp.setAmount(((Number) gramsObj).floatValue());
                    }

                    Object amountObj = ingData.get("IngredientAmount");
                    if (amountObj instanceof String) {
                        recipe.addIngredient(temp, (String) amountObj);
                    }
                }
            }
        }

        getIngredients(recipe.getIngredients().keySet(), true);
        return recipe;
    }


    /**
     * Checks if a recipe for a specific restaurant dish already exists in our database.
     * @param dishId The origin dish ID.
     * @param callback Callback receiving the found Recipe or null.
     */
    public static void checkExistingRecipe(String dishId, RecipeCallback callback) {
        if (dishId == null || dishId.isEmpty()) {
            callback.onDone(null);
            return;
        }

        db.collection(RECIPE_COLLECTION)
                .whereEqualTo("originDish", dishId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        callback.onDone(getRecipe(doc));
                    } else {
                        callback.onDone(null);
                    }
                })
                .addOnFailureListener(e -> callback.onDone(null));
    }

    /**
     * Callback interface for recipe lookups.
     */
    public interface RecipeCallback {
        void onDone(Recipe recipe);
    }

    /**
     * Saves or updates a restaurant and its menu items in Firestore using a batch write.
     * @param restaurant The Restaurant object to sync.
     * @param callback Callback for status return.
     */
    public static void addRestaurant(Restaurant restaurant, Callback callback) {
        WriteBatch batch = db.batch();
        DocumentReference restaurantRef;
        if (restaurant.getId() != null && !restaurant.getId().isEmpty()) {
            restaurantRef = db.collection(RESTAURANT_COLLECTION).document(restaurant.getId());
        } else {
            restaurantRef = db.collection(RESTAURANT_COLLECTION).document();
            restaurant.setId(restaurantRef.getId());
        }

        List<String> dishIds = new ArrayList<>();
        if (restaurant.getDishes() != null) {
            for (RestaurantDish dish : restaurant.getDishes()) {
                DocumentReference dishRef;
                if (dish.getId() != null && !dish.getId().isEmpty()) {
                    dishRef = db.collection(RESTAURANT_DISHES_COLLECTION).document(dish.getId());
                } else {
                    dishRef = db.collection(RESTAURANT_DISHES_COLLECTION).document();
                    dish.setId(dishRef.getId());
                }
                dish.setRestaurantId(restaurant.getId());

                Map<String, Object> dishData = new HashMap<>();
                dishData.put("name", dish.getName());
                dishData.put("description", dish.getDetails());
                dishData.put("price", dish.getPrice());
                dishData.put("restaurantId", restaurant.getId());

                batch.set(dishRef, dishData);
                dishIds.add(dishRef.getId());
            }
        }

        Map<String, Object> restaurantData = new HashMap<>();
        restaurantData.put("name", restaurant.getName());
        restaurantData.put("website", restaurant.getWebsite());

        LatLng location = restaurant.getLocation();
        if (location != null) {
            restaurantData.put("location", new GeoPoint(location.latitude, location.longitude));
        } else {
            restaurantData.put("location", null);
        }

        restaurantData.put("dishIds", dishIds);
        batch.set(restaurantRef, restaurantData);

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onDone("Success"))
                .addOnFailureListener(e -> callback.onDone("Error: " + e.getMessage()));
    }

    /**
     * Updates the physical coordinates of an existing restaurant.
     * @param restaurant The restaurant with updated location data.
     */
    public static void updateRestaurantLocation(Restaurant restaurant) {
        if (restaurant.getId() == null || restaurant.getLocation() == null) return;

        GeoPoint geoPoint = new GeoPoint(restaurant.getLocation().latitude, restaurant.getLocation().longitude);
        db.collection(RESTAURANT_COLLECTION)
                .document(restaurant.getId())
                .update("location", geoPoint)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Restaurant location updated successfully"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error updating restaurant location", e));
    }


    /**
     * Searches for a restaurant in Firestore using its website URL.
     * @param website The URL to match.
     * @param restaurant The object to populate with found data.
     * @param callback Callback for status.
     */
    public static void searchRestaurantByWebsite(String website, Restaurant restaurant, Callback callback) {
        db.collection(RESTAURANT_COLLECTION)
                .whereEqualTo("website", website)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        restaurant.setId(doc.getId());
                        String existingName = doc.getString("name");
                        if (existingName != null && !existingName.isEmpty()) {
                            restaurant.setName(existingName);
                        }
                        restaurant.setWebsite(doc.getString("website"));
                        GeoPoint geoPoint = doc.getGeoPoint("location");
                        if (geoPoint != null) {
                            restaurant.setLocation(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                        }
                        loadRestaurantDishes(restaurant, callback);
                    } else {
                        callback.onDone("Error: No restaurant found");
                    }
                })
                .addOnFailureListener(e -> callback.onDone("Error: " + e.getMessage()));
    }

    /**
     * Fetches all dishes associated with a specific restaurant ID.
     */
    private static void loadRestaurantDishes(Restaurant restaurant, Callback callback) {
        db.collection(RESTAURANT_DISHES_COLLECTION)
                .whereEqualTo("restaurantId", restaurant.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    restaurant.getDishes().clear();
                    for (DocumentSnapshot dishDoc : queryDocumentSnapshots.getDocuments()) {
                        RestaurantDish dish = new RestaurantDish();
                        dish.setId(dishDoc.getId());
                        dish.setName(dishDoc.getString("name"));
                        dish.setDetails(dishDoc.getString("description"));
                        Double price = dishDoc.getDouble("price");
                        dish.setPrice(price != null ? price : 0.0);
                        dish.setRestaurantId(restaurant.getId());
                        restaurant.addDish(dish);
                    }
                    restaurant.setLoaded();
                    callback.onDone("Success");
                })
                .addOnFailureListener(e -> callback.onDone("Failed to load dishes: " + e.getMessage()));
    }
}
