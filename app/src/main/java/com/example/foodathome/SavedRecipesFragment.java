package com.example.foodathome;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the list of recipes saved by the user.
 * Allows users to view or delete their saved recipes.
 */
public class SavedRecipesFragment extends Fragment {

    private ListView lvSavedRecipes;
    private List<Recipe> savedRecipes = new ArrayList<>();
    private RecipeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_recipes, container, false);
        lvSavedRecipes = view.findViewById(R.id.lv_saved_recipes);
        
        // Initialize the RecipeAdapter with the delete click listener.
        adapter = new RecipeAdapter(requireContext(), savedRecipes, recipe -> {
            // When the delete icon is clicked, call FirebaseDataHandler to remove the recipe.
            FirebaseDataHandler.removeRecipeFromUser(recipe.getId(), result -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if ("Success".equals(result)) {
                            // On successful deletion, remove the recipe from the local list and refresh the adapter.
                            savedRecipes.remove(recipe);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "Recipe removed", Toast.LENGTH_SHORT).show();
                        } else {
                            // On failure, show an error toast.
                            Toast.makeText(getContext(), "Error: " + result, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
        lvSavedRecipes.setAdapter(adapter);

        // When a recipe item is clicked, navigate to the RecipeFragment to display its details.
        lvSavedRecipes.setOnItemClickListener((parent, v, position, id) -> {
            Recipe selectedRecipe = savedRecipes.get(position);
            Log.i("myComments","touched recipe: " + selectedRecipe.getName());
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).displaySavedRecipe(selectedRecipe);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("myComments", "onResume called");
        loadSavedRecipes();
    }

    /**
     * Clears the current list of recipes and re-fetches the latest list from Firestore.
     * This ensures the view is up-to-date when the user navigates to the fragment.
     */
    public void loadSavedRecipes() {
        // Clear before loading to avoid duplicates on resume
        savedRecipes.clear();
        FirebaseDataHandler.getUserRecipes(savedRecipes, result -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if ("Success".equals(result)) {
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
