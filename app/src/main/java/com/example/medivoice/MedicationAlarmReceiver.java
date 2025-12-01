package com.example.medivoice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MedicationAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("medicationName");
        String elderName = intent.getStringExtra("elderName");

        if (medName == null) medName = "Medication";
        if (elderName == null) elderName = "Elder";

        String message = "Time to give " + medName + " to " + elderName;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
