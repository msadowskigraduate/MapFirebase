package com.example.sadowsm3.mapfirebase;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import lombok.Builder;

/**
 * Created by sadowsm3 on 11.12.2017.
 */

public class LocationAdapter extends ArrayAdapter<Location> {

    private List<Location> locations;
    private Activity context;
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference mFirebaseDatabase;

    public LocationAdapter(Activity context, List<Location> locations) {
        super(context, R.layout.location_list, locations);
        this.context = context;
        this.locations = locations;
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("locations");
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        final LocationAdapter.ViewHolder viewHolder;
        View rowView = convertView;

        if (rowView == null) {
            final LayoutInflater layoutInflater = context.getLayoutInflater();
            rowView = layoutInflater.inflate(R.layout.location, null, true);
            viewHolder = ViewHolder.builder()
                    .tvLocationName((TextView) rowView.findViewById(R.id.tvLocationTitle))
                    .tvLocationDescription((TextView) rowView.findViewById(R.id.tvLocationDescription))
                    .tvLocationLatitude((TextView) rowView.findViewById(R.id.tvLocationLatitude))
                    .tvLocationLongitude((TextView) rowView.findViewById(R.id.tvLocationLongitude))
                    .tvLocationRadius((TextView) rowView.findViewById(R.id.tvLocationRadius))
                    .cbDelete((CheckBox) rowView.findViewById(R.id.bDelete))
                    .build();

            viewHolder.cbDelete.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Location location = locations.get(position);
                            Log.e(getContext().getClass().getName(), "Deleting item of ID: " + location.getId() + " position: " + position);
                            mFirebaseDatabase.child(location.getId()).removeValue();
                            locations.remove(position);
                            notifyDataSetChanged();
                            Log.e(getContext().getClass().getName(), "Deleted!");
                        }
                    }
            );
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (LocationAdapter.ViewHolder) rowView.getTag();
        }
        Location location = locations.get(position);
        viewHolder.tvLocationName.setText(location.getTitle());
        viewHolder.tvLocationDescription.setText(location.getDescription());
        viewHolder.tvLocationLatitude.setText(String.valueOf(location.getLatitude()));
        viewHolder.tvLocationLongitude.setText(String.valueOf(location.getLongitude()));
        viewHolder.tvLocationRadius.setText(String.valueOf(location.getRadius()));
        return rowView;
    }

    @Builder
    public static class ViewHolder {
        public TextView tvLocationName;
        public TextView tvLocationDescription;
        public TextView tvLocationLatitude;
        public TextView tvLocationLongitude;
        public TextView tvLocationRadius;
        public CheckBox cbDelete;
    }
}

