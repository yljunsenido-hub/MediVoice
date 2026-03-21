package com.example.medivoice;

public class ImageToTextRecord {
    public String name;
    public String text;
    public String date;

    public ImageToTextRecord() {
        // Default constructor required for Firebase
    }

    public ImageToTextRecord(String name, String text, String date) {
        this.name = name;
        this.text = text;
        this.date = date;
    }
}
