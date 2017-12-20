package com.example.sadowsm3.mapfirebase;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private List<Location> locations;
    private LocationAdapter listAdapter;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private ListView listViewProducts;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeGUI();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), MapsActivity2.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(getString(R.string.location_array_bundle), (ArrayList<Location>) locations);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference(getString(R.string.location_array_bundle));

        // store app title to 'app_title' node
        mFirebaseInstance.getReference("android-firebase-database").setValue("Realtime Database");

        mFirebaseInstance.getReference("android-firebase-database").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "App title updated");
                String appTitle = dataSnapshot.getValue(String.class);
                //TODO Set title of activity
                Log.e(TAG, appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });
        // app_title change listener
        mFirebaseInstance.getReference("locations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locations = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Location location = noteDataSnapshot.getValue(Location.class);
                    locations.add(location);
                    Log.e(TAG, location.getTitle());
                    fillListViewData(locations);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFirebaseInstance.getReference(getString(R.string.location_array_bundle)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locations = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Location location = noteDataSnapshot.getValue(Location.class);
                    locations.add(location);
                    fillListViewData(locations);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });

    }
    private void fillListViewData(List<Location> locations) {
        listAdapter = new LocationAdapter(this, locations);
        listViewProducts.setAdapter(listAdapter);
    }

    private void initializeGUI() {
        listViewProducts = (ListView) findViewById(R.id.lvLocations);
        initListViewOnItemClick();
    }

    public void refreshMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void initListViewOnItemClick() {
        listViewProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {
                Location location = locations.get(position);
                Intent intent = new Intent(MainActivity.this, LocationEdit.class);
                intent.putExtra(getString(R.string.location_parcelable_tag), location);
                startActivity(intent);
            }
        });
    }
}
