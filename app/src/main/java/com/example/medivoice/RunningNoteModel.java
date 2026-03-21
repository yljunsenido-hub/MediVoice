package com.example.medivoice;

public class RunningNoteModel {
    public String note;
    public String nurseId;
    public String timestamp;

    public RunningNoteModel() {
    }

    public RunningNoteModel(String note, String nurseId, String timestamp) {
        this.note = note;
        this.nurseId = nurseId;
        this.timestamp = timestamp;
    }
}
