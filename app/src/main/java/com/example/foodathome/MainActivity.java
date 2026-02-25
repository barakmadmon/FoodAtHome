package com.example.foodathome;

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
    private Fragment activeFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AiHandler.init(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        // --- REQUIREMENT: 9.5 ---
        if (savedInstanceState == null) {
            recipeFragment = new RecipeFragment();
            mapFragment = new MapFragment();

            fragmentManager.beginTransaction().add(R.id.fragment_container, recipeFragment, "recipe").hide(recipeFragment).commit();
            fragmentManager.beginTransaction().add(R.id.fragment_container, mapFragment, "map").commit();
            activeFragment = mapFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_map);
        }
        else {
            recipeFragment = (RecipeFragment) fragmentManager.findFragmentByTag("recipe");
            mapFragment = (MapFragment) fragmentManager.findFragmentByTag("map");

            if (recipeFragment != null && !recipeFragment.isHidden()) {
                activeFragment = recipeFragment;
            } else {
                activeFragment = mapFragment;
            }
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment targetFragment = null;

            if (itemId == R.id.nav_recipe) {
                targetFragment = recipeFragment;
            } else if (itemId == R.id.nav_map) {
                targetFragment = mapFragment;
            }

            if (targetFragment != null && targetFragment != activeFragment) {
                fragmentManager.beginTransaction().hide(activeFragment).show(targetFragment).commit();
                activeFragment = targetFragment;
                return true;
            }

            return itemId == R.id.nav_recipe || itemId == R.id.nav_map;
        });
    }

    public void navigateToRecipe(String dishName, String details) {
        if (recipeFragment != null) {
            recipeFragment.loadRecipe(dishName, details, this);
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
