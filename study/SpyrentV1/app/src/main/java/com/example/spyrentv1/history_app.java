package com.example.spyrentv1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spyrentv1.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class history_app extends AppCompatActivity {
    public ListView simpleList;
    private ImageView back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_app_main);

        back = (ImageView)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balik();
            }
        });

        final DatabaseHelper db = new DatabaseHelper(history_app.this);

        Intent i = getIntent();
        final int parentid = i.getIntExtra("session_idx",0);
        final int child = i.getIntExtra("session_id_child",0);
        final String childs_name = i.getStringExtra("child_name");

        String parent_password = db.parent_password(parentid);

        Log.v("data","childs name "+childs_name);

        insertDomain(history_app.this,childs_name,parentid,child);

        new CountDownTimer(3000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("data","remaining for fecthing "+millisUntilFinished);
            }

            @Override
            public void onFinish() {
                ArrayList<String> domain = db.getdomainhistory(parentid,child);
                ArrayList<String> current_time = db.getcurrenttimeDomainhistory(parentid,child);
                simpleList = (ListView) findViewById(R.id.simpleListView);
                final history_app_adapter h  = new history_app_adapter(history_app.this,domain,current_time,parentid,child);
                simpleList.setAdapter(h);
            }
        }.start();



    }

    public void insertDomain(Context context,final String childnames,final int parentid,final int childid){
        RequestQueue queue = Volley.newRequestQueue(context);

        final DatabaseHelper db = new DatabaseHelper(context);

        String url = "http://spyrents.xyz/res_api/select_all_domain_parent.php?child_name="+childnames+" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray array = new JSONArray(response);

                    for(int i = 0; i < array.length(); i++){

                        JSONObject data = array.getJSONObject(i);

                        data.getInt("_id");
                        data.getString("domain");
                        data.getString("current_dates");
                        data.getInt("loginIdparent");
                        data.getInt("loginIdchild");

                        Log.v("data","domain name--------------> "+data.getString("domain"));

                        int count_history_sites = db.checkifSameHistorySite(data.getString("domain"),data.getString("current_dates"));
                        if(count_history_sites != 0){
                            Log.v("data","meron-----------------xxxxxxxxxxxxxxxxxxx");
                        }else{
                            Log.v("data","wala------------------xxxxxxxxxxxxxxxxxxx");

                            long id_history = db.insertHistory_domain(data.getString("domain"),data.getString("current_dates"),parentid,childid);
                            if(id_history>=0){
                                Log.v("data","save successsfully--------------------->site");
                            }else{
                                Log.v("data","don't give up fuckers---------------------->");
                            }
                        }

                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error response parent insert domain ------------------------->"+error);
            }
        });
        queue.add(stringRequest);

    }

    public void balik(){
        Intent intent = new Intent(this, Parent_panel.class);
        startActivity(intent);
    }

}
