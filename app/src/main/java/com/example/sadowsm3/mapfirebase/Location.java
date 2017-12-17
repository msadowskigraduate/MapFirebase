package com.example.sadowsm3.mapfirebase;

import lombok.Builder;
import lombok.Data;

/**
 * Created by sadowsm3 on 17.12.2017.
 */
@Builder
@Data
public class Location {
    String id;
    String title;
    String description;
    float longitude;
    float latitude;
}
