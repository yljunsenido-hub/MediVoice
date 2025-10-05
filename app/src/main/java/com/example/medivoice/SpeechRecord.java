package com.example.medivoice;

public class SpeechRecord {
    public String name;
    public String text;
    public String date;

    public SpeechRecord() {
        // Default constructor required for Firebase
    }

    public SpeechRecord(String name, String text, String date) {
        this.name = name;
        this.text = text;
        this.date = date;
    }
}
