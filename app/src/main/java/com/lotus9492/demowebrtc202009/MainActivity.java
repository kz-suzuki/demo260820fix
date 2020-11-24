package com.lotus9492.demowebrtc202009;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import java.util.UUID;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    Button btnCall;
    TextView textViewID;
    Intent intent;
    EditText editText;
    String uid;
    String subUID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get random 10 digits number uniqueID,
        uid = UUID.randomUUID().toString().replace("-", "").replaceAll("[a-z]", "");
        subUID = uid.substring(0, 10);

        //display this uniqueID to screen
        textViewID = (TextView) findViewById(R.id.txtViewID);
        textViewID.setText(subUID);

        //Log.d("random-uid", uid);
        //Log.d("random-subUID", subUID);

        btnCall = (Button) findViewById(R.id.btnCall);
        editText = (EditText) findViewById(R.id.editText);
        //Log.d("edittext", peerId);

        checkCameraHardware(this);

        btnCall.setOnClickListener(v -> {
            intent = new Intent(this, DisplayCamera.class);
            //intent.putExtra("peerId", subUID);
            startActivity(intent);
        });
    }

    //Detecting camera hardware
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            //Log.d("checkcam", "camera exist");
            // this device has a camera
            return true;
        } else {
            //Log.d("checkcam", "camera not exist");
            // no camera on this device
            return false;
        }
    }

}
