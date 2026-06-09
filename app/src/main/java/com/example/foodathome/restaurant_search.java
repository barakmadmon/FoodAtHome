package com.example.foodathome;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

/**
 * A fragment for searching for a restaurant and displaying its menu.
 */
public class restaurant_search extends Fragment {

    private TextInputEditText etRestaurantLink;
    private Button btnSearch;
    private TextView tvRestaurantName;
    private ListView lvMenu;
    private Restaurant currentRestaurant = null;

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_search, container, false);

        etRestaurantLink = view.findViewById(R.id.et_restaurant_link);
        btnSearch = view.findViewById(R.id.btn_search_restaurant);
        tvRestaurantName = view.findViewById(R.id.tv_restaurant_name);
        lvMenu = view.findViewById(R.id.lv_restaurant_menu);

        btnSearch.setOnClickListener(v -> {
            String website = etRestaurantLink.getText().toString().trim();
            if (TextUtils.isEmpty(website)) {
                Toast.makeText(getContext(), "Please enter a website link", Toast.LENGTH_SHORT).show();
                return;
            }

            currentRestaurant = new Restaurant();

            new Thread( () -> {
                RestaurantHelper.searchResteraunt(currentRestaurant,website);
                getActivity().runOnUiThread(() -> {
                    Log.i("myComments", "Displaying restaurant");
                    displayRestaurant(currentRestaurant);
                });
            }).start();
        });

        lvMenu.setOnItemClickListener((parent, v, position, id) -> {
            RestaurantDish dish = currentRestaurant.getDishes().get(position);
            Log.d("myComments", "Clicked on dish: " + dish.getName());

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToRecipe(dish.getName(), dish.getDetails(), dish.getId(), dish.getPrice(),currentRestaurant.getName());
            }
        });

        return view;
    }

    /**
     * Displays the restaurant's name and menu.
     * @param restaurant The restaurant to display.
     */
    private void displayRestaurant(Restaurant restaurant) {
        tvRestaurantName.setText(restaurant.getName());
        tvRestaurantName.setVisibility(View.VISIBLE);

        if (restaurant.isLoaded() && restaurant.getDishes() != null && !restaurant.getDishes().isEmpty()) {
            ArrayAdapter<RestaurantDish> adapter = new ArrayAdapter<>(getContext(),
                    R.layout.white_text_list_item, restaurant.getDishes());
            lvMenu.setAdapter(adapter);
            lvMenu.setVisibility(View.VISIBLE);
        } else {
            lvMenu.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No dishes found for this restaurant", Toast.LENGTH_SHORT).show();
        }
    }
}