package com.fplay.audioapp.activities;

import android.graphics.Point;

public class AnimalDetail {
    public String name;
    public String audio_url;
    public Point map_cordinates;
    public String sticker_number; //Same as audio file number

    public AnimalDetail(){}
    public AnimalDetail(String n, String audio, Point map, String sticker){
        name = n;audio_url = audio;map_cordinates=map;sticker_number = sticker;
    }
    // copy constructor
    AnimalDetail(AnimalDetail c) {

        name = c.name;
        audio_url = c.audio_url;
        map_cordinates = c.map_cordinates;
        sticker_number = c.sticker_number;
    }

}
