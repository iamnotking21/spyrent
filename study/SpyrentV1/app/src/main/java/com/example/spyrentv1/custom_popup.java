package com.example.spyrentv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.spyrentv1.R;

import java.util.ArrayList;

public class custom_popup extends AppCompatActivity {
    private ListView simpleListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();

        int id = i.getIntExtra("session_idx",0);
        String childid = i.getStringExtra("child_name");
        Log.v("data","session_id------------->"+childid );

        DatabaseHelper db = new DatabaseHelper(custom_popup.this);

        ArrayList<String> child_name = db.childs_name_2(childid,1);
        ArrayList<String> datachildid = db.childs_id_2(childid,1);

        simpleListView = (ListView) findViewById(R.id.simplechilddata);
        final childAccountsAdapter wx = new childAccountsAdapter(getApplicationContext(),child_name,datachildid,id);
        simpleListView.setAdapter(wx);
    }
}
