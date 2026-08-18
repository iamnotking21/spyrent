package com.ax.childapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class dodaily_reciever extends BroadcastReceiver {
    public Vibrator vibrator;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Time Start... Now Vibrating !!!",
                Toast.LENGTH_LONG).show();

        int id = intent.getIntExtra("p_id",1);
        String app_name = intent.getStringExtra("app_name");

        vibrator = (Vibrator) context
                .getSystemService(Context.VIBRATOR_SERVICE);

        vibrator.vibrate(2000);

        Log.v("data","timers start 1111--------------------->"+id);
        ArrayList<Integer> data_id = new ArrayList<>();
        data_id.add(id);

        websiteAdapter db = new websiteAdapter(context);

        for (int i = 0 ; i < data_id.size(); i++){
            update_app_timer_status(db,app_name,3);
        }



    }

    public void update_app_timer_status(websiteAdapter db,String p_uid,int timerstatus){

        int id_count = db.update_block_app_active(p_uid,timerstatus);

        if(id_count>=0){

            Log.v("update","successfully start");

        }else{

            Log.v("not","update");

        }

    }

}
