package com.example.mapsactivity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public FusedLocationProviderClient fusedLocationClient;
    private double locationLat, locationLong;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);

        //check location permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        //get location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.mapstyle_mine));
    }

    @Override
    public void onResume() {
        super.onResume();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null) {
                            // store longitude and latitude
                            locationLong = location.getLongitude();
                            locationLat = location.getLatitude();
                            LatLng currentLocation = new LatLng(locationLat, locationLong);
                            mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here!"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

                            //Calculate distance from our location to wherever we want
                            String destination = "Seattle";
                            List<Address> addresses;
                            try {
                                addresses = geocoder.getFromLocationName(destination, 1);
                                while (addresses.size()==0) {
                                    addresses = geocoder.getFromLocationName(destination, 1);
                                }
                                Address address = addresses.get(0);
                                double destLat = address.getLatitude();
                                double destLong = address.getLongitude();

                                //calculate distance between
                                float[] results = new float[3];
                                Location.distanceBetween(locationLat, locationLong, destLat, destLong, results);
                                results[0] = results[0] * (float) 0.000621;

                                //Make a Toast displaying the distance
                                Context context = getApplicationContext();
                                CharSequence text = "It's " + results[0] + " miles to " + destination;
                                int duration = Toast.LENGTH_LONG;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
    }
}
