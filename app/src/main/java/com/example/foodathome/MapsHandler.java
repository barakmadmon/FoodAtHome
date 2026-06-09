package com.example.foodathome;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles Google Maps and Places API interactions.
 */
public class MapsHandler {
    public static final String TAG = "myMapFragment";
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final int SEARCH_RADIUS = 5000; // 5km
    private static final List<String> LOCATION_FILTER = Arrays.asList("restaurant", "cafe", "meal_takeaway");

    private final MapFragment fragmentCallback;
    private final FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private final PlacesClient placesClient;
    private boolean locationPermissionGranted;
    private Location lastLocation;

    /**
     * Constructor for MapsHandler.
     *
     * @param fragment The fragment that this handler is associated with.
     */
    public MapsHandler(MapFragment fragment) {
        this.fragmentCallback = fragment;

        if (!Places.isInitialized()) {
            String apiKey = fragment.getString(R.string.maps_api_key);
            Places.initialize(fragment.requireContext(), apiKey);
        }

        this.placesClient = Places.createClient(fragment.requireContext());
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(fragment.requireContext());
    }

    /**
     * Checks if the app has been granted location permission.
     *
     * @return True if location permission is granted, false otherwise.
     */
    public boolean hasLocationPermission() {
        return this.locationPermissionGranted;
    }

    /**
     * Sets the location permission status.
     *
     * @param isGranted True if the permission has been granted, false otherwise.
     */
    public void setLocationPermissionGranted(boolean isGranted) {
        this.locationPermissionGranted = isGranted;
    }

    /**
     * Sets up the Google Map with custom styling.
     *
     * @param googleMap The GoogleMap object to be set up.
     */
    public void setUpMap(GoogleMap googleMap) {
        this.mMap = googleMap;
        boolean success = googleMap.setMapStyle(
                new com.google.android.gms.maps.model.MapStyleOptions("[" +
                        "  {" +
                        "    'featureType': 'poi'," +
                        "    'elementType': 'all'," +
                        "    'stylers': [{ 'visibility': 'off' }]" +
                        "  }" +
                        "]".replace("'", "\"")));

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }
    }

    /**
     * Gets the device's last known location and moves the map camera to that location.
     */
    public void getDeviceLocation() {
        try {
            if (this.locationPermissionGranted) {
                Task<Location> locationResult = this.fusedLocationClient.getLastLocation();
                locationResult.addOnSuccessListener(this.fragmentCallback.requireActivity(), location -> {
                    if (this.lastLocation != location) {
                        this.lastLocation = location;
                        if (this.lastLocation != null) {
                            LatLng currentLatLng = new LatLng(this.lastLocation.getLatitude(), this.lastLocation.getLongitude());
                            this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            findAndShowNearbyRestaurants();
                        } else {
                            Log.e(TAG, "Location is null");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
        }
    }

    /**
     * Updates the map's UI settings based on whether location permission has been granted.
     */
    public void updateLocationUI() {
        if (this.mMap != null) {
            try {
                this.mMap.setMyLocationEnabled(this.locationPermissionGranted);
                this.mMap.getUiSettings().setMyLocationButtonEnabled(this.locationPermissionGranted);

                if (!this.locationPermissionGranted) {
                    this.lastLocation = null;
                    getLocationPermission();
                }
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException: " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for location permission if it has not already been granted.
     */
    public void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.fragmentCallback.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            this.locationPermissionGranted = true;
        } else {
            this.fragmentCallback.requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Finds nearby restaurants, shows them on the map, and passes the results to the fragment callback.
     */
    public void findAndShowNearbyRestaurants() {
        if (this.mMap != null && this.lastLocation != null) {
            this.mMap.clear();

            LatLng currentLatLng = new LatLng(this.lastLocation.getLatitude(), this.lastLocation.getLongitude());
            this.mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title("your location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.WEBSITE_URI,
                    Place.Field.LAT_LNG
            );

            CircularBounds circle = CircularBounds.newInstance(currentLatLng, SEARCH_RADIUS);
            SearchNearbyRequest searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
                    .setIncludedTypes(LOCATION_FILTER).setMaxResultCount(20)
                    .build();

            this.placesClient.searchNearby(searchNearbyRequest)
                    .addOnSuccessListener((response) -> {
                        List<Restaurant> restaurants = new ArrayList<>();
                        List<Place> places = response.getPlaces();
                        for (Place place : places) {
                            if (place.getLatLng() != null) {
                                this.mMap.addMarker(new MarkerOptions()
                                        .position(place.getLatLng())
                                        .title(place.getName()));
                            }

                            String name = place.getName();
                            String website = "Not found";
                            if (place.getWebsiteUri() != null) {
                                website = place.getWebsiteUri().toString();
                            }

                            Restaurant restaurant = new Restaurant(name, website);
                            restaurant.setLocation(place.getLatLng());
                            restaurants.add(restaurant);
                        }

                        if (this.lastLocation != null) {
                            LatLng userLatLng = new LatLng(this.lastLocation.getLatitude(), this.lastLocation.getLongitude());
                            restaurants.sort((r1, r2) -> Float.compare(r1.calculateDistance(userLatLng), r2.calculateDistance(userLatLng)));
                        }

                        this.fragmentCallback.onRestaurantsDone(restaurants);
                    })
                    .addOnFailureListener((exception) -> {
                        Log.e(TAG, "Search nearby failed: " + exception.getMessage());
                        this.fragmentCallback.onRestaurantsDone(null);
                    });
        }
    }
}
