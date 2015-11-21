package com.example.it.beaconhack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Toast.makeText(getApplicationContext(), "HOla", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "ADIOS", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "Cambio", Toast.LENGTH_SHORT).show();

        //TO DO: HOLA


    }
}
