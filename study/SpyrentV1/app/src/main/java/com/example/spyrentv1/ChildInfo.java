package com.example.spyrentv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.spyrentv1.R;

public class ChildInfo extends AppCompatActivity {
    private Button blockapp, blocksite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_info2);
        blockapp = (Button) findViewById(R.id.blockapp);
        blocksite = (Button) findViewById(R.id.blocksite);


        blockapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp();
            }
        });

        blocksite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bs();
            }
        });

    }

    public void bp(){
        Intent intent = new Intent(this, Blockapp.class);
        startActivity(intent);
    }

    public void bs(){
        Intent intent = new Intent(this, BlockSite.class);
        startActivity(intent);
    }

}


