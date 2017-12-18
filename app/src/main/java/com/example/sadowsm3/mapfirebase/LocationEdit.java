package com.example.sadowsm3.mapfirebase;


import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LocationEdit extends AppCompatActivity {

    private static final String TAG = LocationEdit.class.getSimpleName();

    ArrayList<Location> locations;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    private String locationId;

    private Button btnSave;
    private Button btnReturn;
    private EditText etName;
    private EditText etPrice;
    private EditText etQuantity;
    private CheckBox cbBought;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_edit);

        initGUI();

        Intent i = getIntent();
        if (i.getExtras() != null && i.getExtras().getParcelable("location") != null) {
            setButtonListener(false);
            Location location = i.getExtras().getParcelable("location");
            try {
                locationId = location.getId();
                setValues(location.getTitle(), String.valueOf(location.getDescription()), String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setButtonListener(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        mFirebaseInstance = FirebaseDatabase.getInstance();

        mFirebaseDatabase = mFirebaseInstance.getReference("locations");

        mFirebaseInstance.getReference("android-firebase-database").setValue("Realtime Database");

        mFirebaseInstance.getReference("android-firebase-database").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "App title updated");
                String appTitle = dataSnapshot.getValue(String.class);
                getSupportActionBar().setTitle(appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });
    }

    /**
     * Creating new user node under 'users'
     */
    public void createProduct(String title, String description,
                              float longitude, float latitude) {

        locationId = mFirebaseDatabase.push().getKey();
        Location location = Location.builder()
                .id(locationId)
                .title(title)
                .description(description)
                .longitude(longitude)
                .latitude(latitude)
                .build();

        mFirebaseDatabase.child(String.valueOf(locationId)).setValue(location);

        addUserChangeListener();
    }

    private void addUserChangeListener() {
        mFirebaseDatabase.child(String.valueOf(locationId)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Location location = dataSnapshot.getValue(Location.class);
                if (location == null) {
                    Log.e(TAG, "Location data is null!");
                    return;
                }
                Log.e(TAG, "Location data is changed!" + location.getTitle());
                fieldReset();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });
    }

    private void updateProduct(String title, String description, float longitude, float latitude) {
        mFirebaseDatabase.child(String.valueOf(locationId)).child("title").setValue(title);
        mFirebaseDatabase.child(String.valueOf(locationId)).child("description").setValue(description);
        mFirebaseDatabase.child(String.valueOf(locationId)).child("longitude").setValue(longitude);
        mFirebaseDatabase.child(String.valueOf(locationId)).child("latitude").setValue(latitude);
    }

    private void initGUI() {
        btnSave = (Button) findViewById(R.id.btnFirebaseSave);
        btnReturn = (Button) findViewById(R.id.btnReturn);
        etName = (EditText) findViewById(R.id.name);
        etPrice = (EditText) findViewById(R.id.price);
        etQuantity = (EditText) findViewById(R.id.quantity);
        cbBought = (CheckBox) findViewById(R.id.cbProductBought);
    }

    private void fieldReset() {
        etName.setText("");
        etPrice.setText("");
        etQuantity.setText("");
        cbBought.setChecked(false);
        btnSave.setText("Save");
    }

    public void returnToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void setValues(String name, String price, String quantity, String isBought) {
        etName.setText(name);
        etPrice.setText(price);
        etQuantity.setText(quantity);
        cbBought.setChecked(Boolean.parseBoolean(isBought));
    }

    private void setButtonListener(final boolean state) {
        if (!state)
            btnSave.setText("Edit");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "";
                String description = "";
                float longitude = 1;
                float latitude = 1;
                try {
                    title = etName.getText().toString();
                    description = etPrice.getText().toString();
                    longitude = Float.valueOf(etQuantity.getText().toString());
                    latitude = Float.valueOf();
                    if (state) {
                        createProduct(title, description, longitude, latitude);
                    } else {
                        updateProduct(title, description, longitude, latitude);
                    }
                    fieldReset();
                } catch (Exception e) {
                    etName.setError("There are errors in your product definition.");
                }

            }
        });
    }
}
