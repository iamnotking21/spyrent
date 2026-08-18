package com.example.spyrentv1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

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

public class content extends AppCompatActivity {
    public ListView simpleList;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        back = (ImageView)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balik();
            }
        });

        final DatabaseHelper db = new DatabaseHelper(content.this);

        Intent i = getIntent();
        final int parentid = i.getIntExtra("session_idx",0);
        final int child = i.getIntExtra("session_id_child",0);
        final String childs_name = i.getStringExtra("child_name");

        //db.del_all_installed_apps(parentid,child);

        insertInstalledApps(content.this,childs_name,parentid,child);

        new CountDownTimer(3000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("data","remaining installed apps time "+millisUntilFinished);
            }

            @Override
            public void onFinish() {
                ArrayList<String> apps_name = db.getapp_name(parentid,child);
                ArrayList<String> apps_pack = db.getapp_packs(parentid,child);

                simpleList = (ListView) findViewById(R.id.simpleListView);
                final CustomAdapter customAdapter= new CustomAdapter(content.this,apps_name,apps_pack,parentid,child);
                simpleList.setAdapter(customAdapter);

            }
        }.start();

    }

    public void insertInstalledApps(Context context,final String childnames,final int parentid,final int childid){
        RequestQueue queue = Volley.newRequestQueue(context);

        final DatabaseHelper db = new DatabaseHelper(context);

        String url = "http://spyrents.xyz/res_api/select_all_installed_apps.php?child_name="+childnames+" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{

                    JSONArray array = new JSONArray(response);

                    for(int i = 0 ; i < array.length(); i++){

                        JSONObject data = array.getJSONObject(i);

                        data.getInt("_id");
                        data.getInt("loginIdparent");
                        data.getInt("loginIdchild");
                        data.getInt("size");
                        data.getString("apps_name");
                        data.getString("apps_pack");

                        boolean val = checkSameNameApp(parentid,childid,data.getString("apps_pack"));
                        if(val==true){
                            Log.v("data","meron");
                        }else{
                            long id = db.insertInstalledDataChild(data.getString("apps_name"),data.getString("apps_pack"),parentid,childid,data.getInt("size"));

                            if(id>=0){
                                //Toast.makeText(MyService.this,"Scanning Complete.........",Toast.LENGTH_SHORT).show();
                                Log.v("data","installed apps childs----------------------> save");
                            }else{
                                Log.v("data","installed apps childs----------------------> not");
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
                Log.v("data","error installed apps ------------->"+error);
            }
        });
        queue.add(stringRequest);

    }

    public boolean checkSameNameApp(int parentid,int childid,String appname){
        DatabaseHelper db = new DatabaseHelper(content.this);
        int count = db.getidsameinstalled(parentid,childid,appname);
        boolean count_val;
        if(count!=0){
            count_val=true;
        }else{
            count_val=false;
        }
        return count_val;
    }
    public void balik(){
        Intent intent = new Intent(this, Parent_panel.class);
        startActivity(intent);
    }
}
