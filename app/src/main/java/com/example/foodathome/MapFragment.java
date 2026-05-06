package com.example.foodathome;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* object for map fragment ui

METHODS:
    - showDishList - set dish to visible
    - showRestaurantList - set restaurant to visible
    + onMapsReady - initialise map with settings
        input: googleMap - map object
    + onRestaurantsDone - update restaurant listview
        input: result - map of restaurants
    + showRestaurantDishes - update dishes listview
        input: restaurant - restaurant object containing dishes

*/
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapFragment";
    private MapsHandler mapsHandler = null;
    
    private ListView restaurantListView;
    private ArrayAdapter<Restaurant> restaurantAdapter;
    private List<Restaurant> restaurantList = new ArrayList<>();
    private Restaurant currentRestaurant = null;

    private View dishesContainer;
    private ListView dishListView;
    private ArrayAdapter<RestaurantDish> dishAdapter;
    private Button backToRestaurantsButton;
    private TextView restaurantTitleTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        restaurantListView = view.findViewById(R.id.restaurantListView);
        restaurantAdapter = new ArrayAdapter<>(requireContext(), R.layout.restaurant_item, restaurantList);
        restaurantListView.setAdapter(restaurantAdapter);

        dishesContainer = view.findViewById(R.id.dishesContainer);
        dishListView = view.findViewById(R.id.dishesListView);
        backToRestaurantsButton = view.findViewById(R.id.backToRestaurantsButton);
        restaurantTitleTextView = view.findViewById(R.id.restaurantTitleTextView);

        backToRestaurantsButton.setOnClickListener(v -> showRestaurantList());

        restaurantListView.setOnItemClickListener((parent, v, position, id) -> {
            currentRestaurant = restaurantList.get(position);
            Log.d(TAG, "Clicked on restaurant: " + currentRestaurant.getName());

            showRestaurantDishes(currentRestaurant);
        });

        dishListView.setOnItemClickListener((parent, v, position, id) -> {
            if (currentRestaurant != null) {
                RestaurantDish dish = currentRestaurant.getDishes().get(position);
                Log.d(TAG, "Clicked on dish: " + dish.getName());

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToRecipe(dish.getName(), dish.getDetails());
                }
            }
        });

        this.mapsHandler = new MapsHandler(this);

        // get map fragment, if not created yet, create it. then load the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_inner);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map_inner, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    private void showDishList() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                dishesContainer.setVisibility(View.VISIBLE);
                restaurantListView.setVisibility(View.GONE);
            });
        }
    }

    private void showRestaurantList() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                dishesContainer.setVisibility(View.GONE);
                restaurantListView.setVisibility(View.VISIBLE);
            });
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("myComments","maps ready");
        if (this.mapsHandler != null) {
            this.mapsHandler.setUpMap(googleMap);
            this.mapsHandler.getLocationPermission();
            this.mapsHandler.updateLocationUI();
            this.mapsHandler.getDeviceLocation();
        }
    }

    public void onRestaurantsDone(List<Restaurant> restaurants) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                restaurantList.clear();
                if (restaurants != null) {
                    restaurantList.addAll(restaurants);
                }

                restaurantAdapter.notifyDataSetChanged();
            });
        }
    }

    private void showRestaurantDishes(Restaurant restaurant) {
        new Thread(() -> {
            if(!restaurant.isLoaded())
                RestaurantHelper.getRestaurantDishes(restaurant);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    restaurantTitleTextView.setText(restaurant.getName());
                    dishAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, restaurant.getDishes());
                    dishListView.setAdapter(dishAdapter);
                });
            }

            showDishList();
        }).start();
    }
}
