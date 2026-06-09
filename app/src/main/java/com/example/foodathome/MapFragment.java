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

import android.content.pm.PackageManager;

/**
 * Fragment for displaying nearby restaurants on a map and in a list.
 * Users can select a restaurant to view its menu.
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

        restaurantAdapter = new ArrayAdapter<>(requireContext(), R.layout.white_text_list_item, restaurantList);
        restaurantListView.setAdapter(restaurantAdapter);

        dishesContainer = view.findViewById(R.id.dishesContainer);
        dishListView = view.findViewById(R.id.dishesListView);
        backToRestaurantsButton = view.findViewById(R.id.backToRestaurantsButton);
        restaurantTitleTextView = view.findViewById(R.id.restaurantTitleTextView);

        backToRestaurantsButton.setOnClickListener(v -> showRestaurantList());

        restaurantListView.setOnItemClickListener((parent, v, position, id) -> {
            currentRestaurant = restaurantList.get(position);
            showRestaurantDishes(currentRestaurant);
        });

        dishListView.setOnItemClickListener((parent, v, position, id) -> {
            if (currentRestaurant != null) {
                RestaurantDish dish = currentRestaurant.getDishes().get(position);
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToRecipe(dish.getName(), dish.getDetails(), dish.getId(), dish.getPrice(),currentRestaurant.getName());
                }
            }
        });

        this.mapsHandler = new MapsHandler(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_inner);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map_inner, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    /**
     * Shows the list of dishes for a selected restaurant and hides the main restaurant list.
     */
    private void showDishList() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                dishesContainer.setVisibility(View.VISIBLE);
                restaurantListView.setVisibility(View.GONE);
            });
        }
    }

    /**
     * Shows the main restaurant list and hides the dish list view.
     */
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
        Log.i("myComments", "open map");
        if (this.mapsHandler != null) {
            Log.i("myComments", "map not null");
            this.mapsHandler.setUpMap(googleMap);
            this.mapsHandler.getLocationPermission();
            this.mapsHandler.updateLocationUI();

            if (mapsHandler.hasLocationPermission()) {
                startLocationUpdates();
            }
        }
    }

    /**
     * Callback method to update the restaurant list when data is fetched.
     * @param restaurants The list of restaurants to display.
     */
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

    /**
     * Fetches and displays the dishes for a given restaurant.
     * @param restaurant The restaurant whose dishes will be shown.
     */
    private void showRestaurantDishes(Restaurant restaurant) {
        new Thread(() -> {
            RestaurantHelper.searchResteraunt(restaurant, restaurant.getWebsite());

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    restaurantTitleTextView.setText(restaurant.getName());
                    dishAdapter = new ArrayAdapter<>(requireContext(), R.layout.white_text_list_item, restaurant.getDishes());
                    dishListView.setAdapter(dishAdapter);
                });
            }
            showDishList();
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MapsHandler.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (mapsHandler != null) {
                mapsHandler.setLocationPermissionGranted(granted);
                mapsHandler.updateLocationUI();
                if (granted) {
                    startLocationUpdates();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Starts a background thread to periodically fetch the device's location.
     */
    public void startLocationUpdates() {
        new Thread(() -> {
            while (true) {
                if (mapsHandler != null && mapsHandler.hasLocationPermission() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> mapsHandler.getDeviceLocation());
                }
                try {
                    Thread.sleep(5000);
                    Log.i(TAG, "SEARCHING AGAIN");
                } catch (InterruptedException e) {
                    Log.e(TAG, "Location update thread interrupted", e);
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                    break; // Exit the loop
                }
            }
        }).start();
    }
}