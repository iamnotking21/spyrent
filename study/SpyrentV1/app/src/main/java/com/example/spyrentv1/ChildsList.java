package com.example.spyrentv1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spyrentv1.R;

import java.util.ArrayList;

public class ChildsList extends AppCompatActivity {
    private ImageView back;
    private ListView simpleListView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.childslistview);

        Intent i = getIntent();
        int id = i.getIntExtra("session_idx",0);
        String childid = i.getStringExtra("child_name");
        Log.v("data","session_id------------->"+childid );
        back = (ImageView)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balik();
            }
        });

        DatabaseHelper db = new DatabaseHelper(ChildsList.this);

        ArrayList<String> child_name = db.childs_name_2(childid,1);
        ArrayList<String> datachildid = db.childs_id_2(childid,1);

        simpleListView = (ListView) findViewById(R.id.simplechilddata);
        final childAccountsAdapter wx = new childAccountsAdapter(getApplicationContext(),child_name,datachildid,id);
        simpleListView.setAdapter(wx);


    }

    public void balik(){
        Intent intent = new Intent(this, Parent_panel.class);
        startActivity(intent);
    }
}
