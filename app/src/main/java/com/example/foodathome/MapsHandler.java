package com.example.foodathome;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/* METHODS:
    + setUpMap - initialise map with settings
        input: googleMap- map object
    + getDeviceLocation - get the current location of device
    + updateLocationUI - set map ui to show current location
    + getLocationPermission -
 */

public class MapsHandler {
    public static final String TAG = "myMapFragment";
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final int SEARCH_RADIUS = 10000; // 10km
    private static final List<String> LOCATION_FILTER = Arrays.asList("restaurant", "cafe", "meal_takeaway");

    private MapFragment fragmentCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private PlacesClient placesClient;
    private boolean locationPermissionGranted;
    private Location lastLocation;

    public MapsHandler(MapFragment fragment) {
        this.fragmentCallback = fragment;

        if (!Places.isInitialized()) {
            String apiKey = fragment.getString(R.string.maps_api_key);
            Places.initialize(fragment.requireContext(), apiKey);
        }

        this.placesClient = Places.createClient(fragment.requireContext());
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(fragment.requireContext());
    }

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

    public void getDeviceLocation() {
        try {
            if (this.locationPermissionGranted) {
                Task<Location> locationResult = this.fusedLocationClient.getLastLocation();
                locationResult.addOnSuccessListener(this.fragmentCallback.requireActivity(), location -> {
                    this.lastLocation = location;
                    Log.i(TAG, "Device location: " + this.lastLocation.getLatitude() + ", " + this.lastLocation.getLongitude() + " ");
                    if (this.lastLocation != null) {
                        LatLng currentLatLng = new LatLng(this.lastLocation.getLatitude(), this.lastLocation.getLongitude());
                        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    }

                    showNearbyRestaurants();
                    findNearbyRestaurants();
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
        }
    }

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

    public void showNearbyRestaurants () {
        if (this.mMap != null && this.lastLocation != null) {
            this.mMap.clear();

            LatLng currentLatLng = new LatLng(this.lastLocation.getLatitude(), this.lastLocation.getLongitude());
            this.mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title("your location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
            CircularBounds circle = CircularBounds.newInstance(currentLatLng, SEARCH_RADIUS);

            SearchNearbyRequest searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
                    .setIncludedTypes(LOCATION_FILTER)
                    .build();


            this.placesClient.searchNearby(searchNearbyRequest)
                    .addOnSuccessListener((response) -> {
                        List<Place> places = response.getPlaces();
                        for (Place place : places) {
                            if (place.getLatLng() != null) {
                                this.mMap.addMarker(new MarkerOptions()
                                        .position(place.getLatLng())
                                        .title(place.getName()));
                            }

                        }
                    })
                    .addOnFailureListener((exception) -> {
                        Log.e(TAG, "Search nearby failed: " + exception.getMessage());
                    });
        }
    }

    public void findNearbyRestaurants() {
        if (this.lastLocation != null) {

            LatLng currentLatLng = new LatLng(this.lastLocation.getLatitude(), this.lastLocation.getLongitude());
            CircularBounds circle = CircularBounds.newInstance(currentLatLng, SEARCH_RADIUS);

            List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.WEBSITE_URI,
                    Place.Field.LAT_LNG
            );

            SearchNearbyRequest searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
                    .setIncludedTypes(LOCATION_FILTER)
                    .build();

            this.placesClient.searchNearby(searchNearbyRequest)
                    .addOnSuccessListener((response) -> {
                        List<Restaurant> restaurants = new ArrayList<>();
                        for (Place place : response.getPlaces()) {
                            String name = place.getName();
                            String website = "Not found";
                            place.getLatLng();
                            if (place.getWebsiteUri() != null) {
                                website = place.getWebsiteUri().toString();
                            }

                            Restaurant restaurant = new Restaurant(name,website);
                            restaurant.setLocation(place.getLatLng());
                            restaurants.add(restaurant);

                        }
                        this.fragmentCallback.onRestaurantsDone(restaurants);
                    })
                    .addOnFailureListener((exception) -> {
                        Log.e(TAG, "Search nearby failed for text list: " + exception.getMessage());
                        this.fragmentCallback.onRestaurantsDone(null);
                    });
        }
    }

    /*
    public void handleRequestPermissionResult(int requestCode, @NonNull int[] grantResults) {
        this.locationPermissionGranted = false;
        if (requestCode ==  this.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.locationPermissionGranted = true;
            }
        }
        this.updateLocationUI();
        this.getDeviceLocation();
    }*/


}
