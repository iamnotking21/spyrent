package com.ax.childapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class end_receiver extends BroadcastReceiver {
    public Vibrator vibrator;
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Time Up... Now Vibrating !!!",
                Toast.LENGTH_LONG).show();

       final  int id = intent.getIntExtra("p_id",1);
       final int parentid = intent.getIntExtra("parentid",2);
        final int childid = intent.getIntExtra("childid",3);

        final int loginIdparent = intent.getIntExtra("loginIdparent",0);
        final int loginIdchild = intent.getIntExtra("loginIdchild",0);

        final String app_name = intent.getStringExtra("app_name");

        del_online_have(context,loginIdparent,loginIdchild,app_name);

        vibrator = (Vibrator) context
                .getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);

        Log.v("data","timers end 222--------------------->"+id+" parentid------->"+parentid+" childid---------------->"+childid);

        final websiteAdapter db = new websiteAdapter(context);
        new CountDownTimer(5000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("data","---------------------->"+millisUntilFinished);
            }
            @Override
            public void onFinish() {
                update_app_timer_status(db,app_name,parentid,childid,4);
            }
        };



    }
    public void del_online_have(Context context, final int parentid, final int childid,final String app_namex){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrent.online/res_api/delete_haveTimer_online.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response have timer ----------------------------------->"+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error response online have timer ---------------------->"+error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();

                params.put("parentid",String.valueOf(parentid));
                params.put("childid",String.valueOf(childid));
                params.put("appname",app_namex);

                return params;
            }
        };
        queue.add(stringRequest);

    }


    public void update_app_timer_status(websiteAdapter db,String p_uid,int parentid,int childid,int timerstatus){
        /*
        int id_count = db.update_block_app_active(p_uid,timerstatus);

        if(id_count>=0){

            Log.v("update","successfully start");

        }else{

            Log.v("not","update");

        }*/

        db.delete_have_timer(p_uid);



    }


}
