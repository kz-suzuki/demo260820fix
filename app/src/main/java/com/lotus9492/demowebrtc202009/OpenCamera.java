package com.lotus9492.demowebrtc202009;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class OpenCamera extends AppCompatActivity {

    Button btnAnswer;
    Button btnDecline;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);

         btnAnswer =(Button)findViewById(R.id.btnAnswer);

         btnAnswer.setOnClickListener(v -> {

             intent = new Intent(this,DisplayCamera.class);
             startActivity(intent);
         });

         btnDecline =(Button)findViewById(R.id.btnDecline);
         intent = null;
         btnDecline.setOnClickListener(v ->{
             intent = new Intent(this, MainActivity.class);
             startActivity(intent);
         });

    }
}
