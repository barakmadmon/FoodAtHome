package com.example.foodathome;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    private FragmentManager fragmentManager;
    private RecipeFragment recipeFragment;
    private MapFragment mapFragment;
    private restaurant_search restaurantSearchFragment;
    private SavedRecipesFragment savedRecipesFragment;
    private UserFragment userFragment;
    private Fragment activeFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectUser();
        setContentView(R.layout.activity_main);

        AiHandler.init(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            recipeFragment = new RecipeFragment();
            mapFragment = new MapFragment();
            restaurantSearchFragment = new restaurant_search();
            savedRecipesFragment = new SavedRecipesFragment();
            userFragment = new UserFragment();

            fragmentManager.beginTransaction().add(R.id.fragment_container, recipeFragment, "recipe").hide(recipeFragment).commit();
            fragmentManager.beginTransaction().add(R.id.fragment_container, restaurantSearchFragment, "search").hide(restaurantSearchFragment).commit();
            fragmentManager.beginTransaction().add(R.id.fragment_container, savedRecipesFragment, "saved").hide(savedRecipesFragment).commit();
            fragmentManager.beginTransaction().add(R.id.fragment_container, userFragment, "user").hide(userFragment).commit();
            fragmentManager.beginTransaction().add(R.id.fragment_container, mapFragment, "map").commit();
            
            activeFragment = mapFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_map);
        }
        else {
            recipeFragment = (RecipeFragment) fragmentManager.findFragmentByTag("recipe");
            mapFragment = (MapFragment) fragmentManager.findFragmentByTag("map");
            restaurantSearchFragment = (restaurant_search) fragmentManager.findFragmentByTag("search");
            savedRecipesFragment = (SavedRecipesFragment) fragmentManager.findFragmentByTag("saved");
            userFragment = (UserFragment) fragmentManager.findFragmentByTag("user");

            if (recipeFragment != null && !recipeFragment.isHidden()) {
                activeFragment = recipeFragment;
            } else if (restaurantSearchFragment != null && !restaurantSearchFragment.isHidden()) {
                activeFragment = restaurantSearchFragment;
            } else if (savedRecipesFragment != null && !savedRecipesFragment.isHidden()) {
                activeFragment = savedRecipesFragment;
            } else if (userFragment != null && !userFragment.isHidden()) {
                activeFragment = userFragment;
            }
            else {
                activeFragment = mapFragment;
            }
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment targetFragment = null;

            if (itemId == R.id.nav_recipe) {
                targetFragment = recipeFragment;
            } else if (itemId == R.id.nav_saved) {
                targetFragment = savedRecipesFragment;
                savedRecipesFragment.loadSavedRecipes();
            } else if (itemId == R.id.nav_map) {
                targetFragment = mapFragment;
            } else if (itemId == R.id.nav_restaurant) {
                targetFragment = restaurantSearchFragment;
            } else if (itemId == R.id.nav_user) {
                targetFragment = userFragment;
                userFragment.updateUser();
            }

            if (targetFragment != null && targetFragment != activeFragment) {
                fragmentManager.beginTransaction().hide(activeFragment).show(targetFragment).commit();
                activeFragment = targetFragment;
                return true;
            }

            return false;
        });
    }

    public void connectUser() {
        startActivity(new Intent(this, LoginActivity.class));
        Log.i("myComments", "connecting user");
    }

    public void disconnectUser() {
        FirebaseDataHandler.signoutUser();
        connectUser();
    }

    /**
     * Navigates to the recipe fragment and triggers the loading of a new recipe.
     * @param dishName The name of the dish.
     * @param details Additional details or description of the dish.
     * @param dishId The unique identifier of the dish.
     * @param price The price of the dish.
     * @param originRestaurant the restaurant the dish is from.
     */
    public void navigateToRecipe(String dishName, String details, String dishId, double price,String originRestaurant) {
        if (recipeFragment != null) {
            recipeFragment.loadRecipe(dishName, details, dishId, price,originRestaurant,this);
            fragmentManager.beginTransaction().hide(activeFragment).show(recipeFragment).commit();
            activeFragment = recipeFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_recipe);
        }
    }

    /**
     * Displays a previously saved recipe in the recipe fragment.
     * @param recipe The Recipe object to display.
     */
    public void displaySavedRecipe(Recipe recipe) {
        if (recipeFragment != null) {
            recipeFragment.displayRecipe(recipe);
            fragmentManager.beginTransaction().hide(activeFragment).show(recipeFragment).commit();
            activeFragment = recipeFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_recipe);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}