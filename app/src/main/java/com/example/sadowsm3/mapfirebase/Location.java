package com.example.sadowsm3.mapfirebase;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by sadowsm3 on 17.12.2017.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location implements Parcelable{
    private String id;
    private String title;
    private String description;
    private float longitude;
    private float latitude;
    private float radius;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeFloat(longitude);
        parcel.writeFloat(latitude);
        parcel.writeFloat(radius);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public Location(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        longitude = in.readFloat();
        latitude = in.readFloat();
        radius = in.readFloat();
    }
}
