package com.example.sadowsm3.mapfirebase;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static java.util.stream.Collectors.toList;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity2.class.getSimpleName();

    private int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private GoogleMap googleMap;
    private LocationRequest mLocationRequest;
    //private ListView listView;
    private boolean mLocationPermissionGranted;
    private List<com.example.sadowsm3.mapfirebase.Location> locationList;
    private LatLng currentLocation;

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        try {
            Bundle bundle = getIntent().getExtras();
            locationList = bundle.getParcelableArrayList(getString(R.string.location_array_bundle));
            if(locationList == null) {
                locationList = new ArrayList<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Errors with parcelable ");
            e.printStackTrace();
        }
        FloatingActionButton clickButton = (FloatingActionButton) findViewById(R.id.placesList);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MapsActivity2.this);
                dialog.setContentView(R.layout.location_list);
                dialog.setTitle("Location List");
                ListAdapter adapter = new LocationAdapter(MapsActivity2.this, locationList);
                ListView lv = (ListView) dialog.findViewById(R.id.lvLocations);
                lv.setAdapter(adapter);
                dialog.show();
                FloatingActionButton addNewButton = (FloatingActionButton) dialog.findViewById(R.id.btnAddNew);
                addNewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getBaseContext(), LocationEdit.class);
                        try {

                            intent.putExtra("longitude", getCurrentLocation().longitude);
                            intent.putExtra("latitude", getCurrentLocation().latitude);
                            startActivity(intent);
                        }
                        catch(Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        com.example.sadowsm3.mapfirebase.Location location = locationList.get(position);
                        centerCameraOnLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                        dialog.hide();
                    }
                });
                lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                        Intent intent = new Intent(getBaseContext(), LocationEdit.class);
                        intent.putExtra(getString(R.string.location_parcelable_tag), locationList.get(position));
                        startActivity(intent);
                        return false;
                    }
                });
            }
        });

        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
        mGeofencingClient = LocationServices.getGeofencingClient(this);
    }

    // Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        centerCameraOnLocation(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentLocation = latLng;
        updatePositionOnMap(latLng);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            centerCameraOnLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMaps) {
        this.googleMap = googleMaps;
        getLocationPermission();
        if (mLocationPermissionGranted) {
            googleMap.setMyLocationEnabled(true);
        }

        if (locationList != null) {
            if(!locationList.isEmpty())
                populateMarkers(locationList);
        } else {
            Toast.makeText(getApplicationContext(), "No locations saved!", Toast.LENGTH_LONG).show();
        }
        startLocationUpdates();
        if(locationList != null)
         populateGeofenceList(locationList);
    }

    private void updatePositionOnMap(LatLng position) {
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions().position(position).title("Current Position"));
        } else {
            Log.e(this.getLocalClassName(), "googleMap is null!");
        }
    }

    private void centerCameraOnLocation(LatLng position) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(position)
                    .zoom(20.0f)
                    .build()));
            googleMap.addMarker(new MarkerOptions().position(position).title("Current Position"));
        } else {
            Log.e(this.getLocalClassName(), "googleMap is null!");
        }
    }


    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    private void populateMarkers(List<com.example.sadowsm3.mapfirebase.Location> locations) {
        for (com.example.sadowsm3.mapfirebase.Location l : locations) {
            googleMap.addMarker(new MarkerOptions().position(new LatLng(l.getLatitude(), l.getLongitude())).title(l.getTitle()));
            drawGeofenceCircle(new LatLng(l.getLatitude(), l.getLongitude()), l.getRadius());
        }
    }

    private LatLng getCurrentLocation() {
        return currentLocation;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    @SuppressLint("MissingPermission")
    private void populateGeofenceList(List<com.example.sadowsm3.mapfirebase.Location> locationList) {
        if(mGeofenceList.size() > 0) {
            removeGeofences();
        }
        Log.e(TAG, "GeofenceList cleared!");

        for (com.example.sadowsm3.mapfirebase.Location l : locationList) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(l.getId() + "###" + l.getTitle())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            l.getLatitude(),
                            l.getLongitude(),
                            l.getRadius()
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(9999999)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
            Log.e(TAG, "Geofance constructed!");

            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeoFenceIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void drawGeofenceCircle(LatLng position, float radius) {
        CircleOptions circleOptions = new CircleOptions()
                .center(position)
                .radius(radius)
                .strokeWidth(2)
                .fillColor(Color.TRANSPARENT)
                .strokeColor(Color.BLUE);

        googleMap.addCircle(circleOptions);
    }

    private void removeGeofences() {
        List<String> geoIds = mGeofenceList.stream()
                .map(Geofence::getRequestId)
                .collect(toList());
        Log.e(TAG, geoIds.size() + " Removing geofences...");
        mGeofencingClient.removeGeofences(geoIds);
        Log.e(TAG, "Removed.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeGeofences();
    }
}
