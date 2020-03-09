package com.example.nachum.pap;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainMap extends AppCompatActivity {

// TODO: turn into a fragment and enable adding new places

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
    }

    /** Called when the user taps the Send button */
    public void goToMap(View view) {
        Intent intent = new Intent(this, NAVDRAW.class);
        intent.putExtra("num", view.getTag().toString());
        startActivity(intent);
    }

}
