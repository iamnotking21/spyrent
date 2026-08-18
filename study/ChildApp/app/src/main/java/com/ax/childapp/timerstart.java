package com.ax.childapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static android.content.Context.ALARM_SERVICE;

public class timerstart extends BroadcastReceiver {
    public static final String data = "start timer";
    private int parentid=12,childid=1,status=1,am_pm_c,soras,smins,sformat,p_uid;
    public Calendar calendar;

    private String app_name;

    @SuppressLint("WrongConstant")
    @Override
    public void onReceive(Context context, Intent intent) {
        //gumawa ng return value para sa array list
        ArrayList<Integer> data_id = new ArrayList<>();
        websiteAdapter db = new websiteAdapter(context);
        Cursor cursor = db.fetch(1);
        if (cursor.moveToFirst()){
            do{
                p_uid = cursor.getInt(cursor.getColumnIndex("p_id"));
                soras = cursor.getInt(cursor.getColumnIndex("soras"));
                smins = cursor.getInt(cursor.getColumnIndex("smins"));
                sformat = cursor.getInt(cursor.getColumnIndex("stimeset"));
                app_name = cursor.getString(cursor.getColumnIndex("apps_name"));
                Log.v(data,"vallll timer "+soras+":"+smins+" "+sformat);
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR,soras);
                calendar.set(Calendar.MINUTE,smins);
                //kailangan i set kung am/pm through database
                calendar.set(Calendar.AM_PM,sformat);
                am_pm_c = calendar.get(Calendar.AM_PM);
                Log.v(data,"P_UID------------->"+p_uid);

                if(p_uid!=0){
                    data_id.add(p_uid);

                    if(am_pm_c!=calendar.get(Calendar.AM)){
                        Log.v(data,"P_UID "+p_uid);
                        Log.v(data,"timer am "+am_pm_c+" "+soras+":"+smins+" "+sformat);
                        //update_app_timer_status(db,p_uid,parentid,childid,3);

                        for(int i = 0 ; i < data_id.size(); i++){
                            startOfTimer(calendar,data_id.get(i),context,app_name);
                        }


                    }else{
                        Log.v(data,"P_UID "+p_uid);
                        //update_app_timer_status(db,p_uid,parentid,childid,3);

                        for(int ix = 0 ; ix < data_id.size(); ix++){
                            startOfTimer(calendar,data_id.get(ix),context,app_name);
                        }
                        Log.v(data,"timer pm "+am_pm_c+" "+soras+":"+smins+" "+sformat);
                    }
                }


            }while(cursor.moveToNext());
        }
        cursor.close();
        //Toast.makeText(context,"timer start", Toast.LENGTH_LONG).show();
    }

    public void startOfTimer(Calendar calendar,int p_id,Context context,String app_name){
        Intent intent = new Intent(context,dodaily_reciever.class);
        intent.setAction(Long.toString(System.currentTimeMillis()));

        intent.putExtra("p_id",p_id);
        intent.putExtra("app_name",app_name);
        Log.v("data","p_id "+p_id);

        int dummyuniqueInt = new Random().nextInt(543254);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),dummyuniqueInt,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


}
