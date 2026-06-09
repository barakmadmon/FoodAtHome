package com.example.foodathome;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

public class IngredientAdapter extends ArrayAdapter<Map.Entry<Ingredient, String>> {

    public IngredientAdapter(@NonNull Context context, @NonNull List<Map.Entry<Ingredient, String>> ingredients) {
        super(context, 0, ingredients);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ingredient_item, parent, false);
        }

        Map.Entry<Ingredient, String> entry = getItem(position);
        Ingredient ingredient = entry.getKey();
        String amount = entry.getValue();

        TextView nameTV = convertView.findViewById(R.id.ingredientNameTV);
        TextView amountTV = convertView.findViewById(R.id.ingredientAmountTV);
        TextView priceTV = convertView.findViewById(R.id.ingredientPriceTV);

        nameTV.setText(ingredient.getName());
        amountTV.setText(amount);
        priceTV.setText(String.format("%.2f", ingredient.getPrice()));

        nameTV.setTextColor(Color.WHITE);
        amountTV.setTextColor(Color.WHITE);
        priceTV.setTextColor(Color.WHITE);

        return convertView;
    }
}