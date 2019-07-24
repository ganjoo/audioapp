package com.fplay.audioapp.utils;


import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class AudioPointDetails {
    public String name;
    public String audio_url;
    public Point map_cordinates;
    public String sticker_number; //Same as audio file number
    public Drawable image_drawable;

    public AudioPointDetails(){}
    public AudioPointDetails(String n, String audio, Point map, String sticker, Drawable imdraw){
        name = n;audio_url = audio;map_cordinates=map;sticker_number = sticker;image_drawable=imdraw;
    }
    // copy constructor
    AudioPointDetails(AudioPointDetails c) {

        name = c.name;
        audio_url = c.audio_url;
        map_cordinates = c.map_cordinates;
        sticker_number = c.sticker_number;
        image_drawable = c.image_drawable;
    }

}