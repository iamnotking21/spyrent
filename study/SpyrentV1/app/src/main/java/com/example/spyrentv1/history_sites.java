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

public class history_sites extends AppCompatActivity {
    private ImageView back;
    public ListView simpleList;
    public String countryList[] = {"tinder","facebook","youtube","chrome","opera mini"};
    public String data[] = {"nov. 30 2019","nov. 30 2019","nov. 30 2019","nov. 30 2019","nov. 30 2019"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_sites_main);

        back = (ImageView)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balik();
            }
        });

        final DatabaseHelper db = new DatabaseHelper(history_sites.this);

        Intent i = getIntent();
        final int parentid = i.getIntExtra("session_idx",0);
        final int child = i.getIntExtra("session_id_child",0);
        final String childs_name = i.getStringExtra("child_name");

        insertapp_history(history_sites.this,childs_name,parentid,child);

        new CountDownTimer(3000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("data","remaining time history app "+millisUntilFinished);
            }

            @Override
            public void onFinish() {
                ArrayList<String> app_name = db.getapphistory(parentid,child);
                ArrayList<String> app_dates = db.getapphistoryCurrentTime(parentid,child);

                simpleList = (ListView) findViewById(R.id.simpleListView);
                final history_sites_adapter hs = new history_sites_adapter(history_sites.this,app_name,app_dates,parentid,child);
                simpleList.setAdapter(hs);

            }
        }.start();

    }

    public void insertapp_history(Context context, final String childnames, final int parentid, final int childid){
        RequestQueue queue = Volley.newRequestQueue(context);

        final DatabaseHelper db = new DatabaseHelper(context);

        String url = "http://spyrents.xyz/res_api/select_all_history_app_parent.php?child_name="+childnames+" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray array = new JSONArray(response);

                    for(int i = 0; i < array.length(); i++){

                        JSONObject data = array.getJSONObject(i);

                        data.getInt("_id");
                        data.getString("apps_name");
                        data.getString("current_dates");
                        data.getInt("loginIdparent");
                        data.getInt("loginIdchild");

                        //Log.v("data","domain name--------------> "+data.getString("domain"));

                        int count_histtory_app = db.checkIfsameHistoryApp(data.getString("apps_name"),data.getString("current_dates"));
                        if(count_histtory_app != 0){
                            Log.v("data","meron");
                        }else{
                            Log.v("data","wala");

                            long id = db.insertHistory_app(data.getString("apps_name"),data.getString("current_dates"),parentid,childid);
                            if(id>=0){
                                Log.v("data","successfully save app history---------------------->"+data.getString("apps_name")+" ---->"+data.getString("current_dates"));
                            }else{
                                Log.v("data","not successsuflyl save don;t fucking give up fuckiers bitch");
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
