package com.example.foodathome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Custom adapter for displaying Recipe objects in a ListView.
 * Includes a delete option to remove recipes from user favorites.
 */
public class RecipeAdapter extends ArrayAdapter<Recipe> {

    /**
     * Interface definition for a callback to be invoked when a delete button is clicked.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(Recipe recipe);
    }

    private OnDeleteClickListener onDeleteClickListener;

    /**
     * Constructor for RecipeAdapter.
     * @param context The current context.
     * @param recipes The list of recipes to display.
     * @param listener Listener for delete button clicks.
     */
    public RecipeAdapter(@NonNull Context context, @NonNull List<Recipe> recipes, OnDeleteClickListener listener) {
        super(context, 0, recipes);
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.restaurant_item, parent, false);
        }

        Recipe currentRecipe = getItem(position);

        TextView recipeNameAndOriginTextView = listItemView.findViewById(R.id.restaurantNameTV);
        if (currentRecipe != null) {
            String displayText = currentRecipe.getName();
            if (currentRecipe.getOriginRestaurant() != null && !currentRecipe.getOriginRestaurant().isEmpty()) {
                displayText += " (from " + currentRecipe.getOriginRestaurant() + ")";
            }
            recipeNameAndOriginTextView.setText(displayText);
        }

        ImageButton deleteBtn = listItemView.findViewById(R.id.deleteRecipeButton);
        deleteBtn.setOnClickListener(v -> {
            if (onDeleteClickListener != null && currentRecipe != null) {
                onDeleteClickListener.onDeleteClick(currentRecipe);
            }
        });

        return listItemView;
    }
}
