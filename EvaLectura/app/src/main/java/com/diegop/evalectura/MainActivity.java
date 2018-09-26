package com.diegop.evalectura;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(MainActivity.this, "" + isChecked, Toast.LENGTH_SHORT).show();
            }
        });

        final CheckedTextView ctv = (CheckedTextView) findViewById(R.id.checkedtextview);
        ctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv.toggle();
            }
        });


    }
}
