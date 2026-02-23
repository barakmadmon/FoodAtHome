package com.example.foodathome;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private MapsHandler mapsHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        this.mapsHandler = new MapsHandler(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mapsHandler.setUpMap(googleMap);
        this.mapsHandler.getLocationPermission();
        this.mapsHandler.updateLocationUI();
        this.mapsHandler.getDeviceLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        this.mapsHandler.handleRequestPermissionResult(requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onRestaurantsDone(String result) {
        Log.d(TAG, "Nearby Restaurants:");
        onDone(result);
    }

    private void onDone(String message) {
        Log.i(TAG, "Operation completed: " + message);
    }
}
