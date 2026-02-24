package com.example.foodathome;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapFragment";
    private MapsHandler mapsHandler = null;
    private ListView restaurantListView;
    private ArrayAdapter<Restaurant> adapter;
    private List<Restaurant> restaurantList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        restaurantListView = view.findViewById(R.id.restaurantListView);
        // Using a simple layout, but you might want a custom adapter for Restaurant objects later
        adapter = new ArrayAdapter<>(requireContext(), R.layout.restaurant_item, restaurantList);
        restaurantListView.setAdapter(adapter);

        this.mapsHandler = new MapsHandler(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_inner);
        
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map_inner, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (this.mapsHandler != null) {
            this.mapsHandler.setUpMap(googleMap);
            this.mapsHandler.getLocationPermission();
            this.mapsHandler.updateLocationUI();
            this.mapsHandler.getDeviceLocation();
        }
    }

    public void onRestaurantsDone(Map<String,String> result) {
        Log.d(TAG, "Nearby Restaurants results received");
        
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                restaurantList.clear();
                for (Map.Entry<String,String> entry : result.entrySet()) {
                    Restaurant restaurant = new Restaurant(entry.getKey(), entry.getValue());
                    restaurantList.add(restaurant);
                }
                adapter.notifyDataSetChanged();
            });
        }
    }

    private void onDone(String message) {
        Log.i(TAG, "Operation completed: " + message);
    }
}
