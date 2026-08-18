package com.ax.childapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

public class endtimer extends Service {
    public static final String data = "End timer";
    private int parentid=12,childid=1,status=1,am_pm_c,oras,mins,format,p_uid,timestatus;
    public Calendar calendar;
    public endtimer() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        endalert();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("WrongConstant")
    public void endalert(){
        websiteAdapter db = new websiteAdapter(endtimer.this);

        Cursor cursor = db.fetch(1);
        if(cursor.moveToFirst()){
            do{
                p_uid = cursor.getInt(cursor.getColumnIndex("p_id"));
                oras = cursor.getInt(cursor.getColumnIndex("oras"));
                mins = cursor.getInt(cursor.getColumnIndex("mins"));
                format = cursor.getInt(cursor.getColumnIndex("timeset"));

                timestatus = cursor.getInt(3);

                Log.v(data,"vall timer end "+oras+":"+mins+" "+format);

                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR,oras);
                calendar.set(Calendar.MINUTE,mins);
                calendar.set(Calendar.AM_PM,format);
                //kailangan i set kung am/pm through database
                am_pm_c = calendar.get(Calendar.AM_PM);
                Log.v(data,"P_UID am---------------------------> "+p_uid);
                if(am_pm_c!=calendar.get(Calendar.AM)){
                    Log.v(data,"timer am "+am_pm_c+"  "+oras+":"+mins+" "+format);
                    Log.v(data,"P_UID am "+p_uid);

                    //update_app_timer_status(db,p_uid,parentid,childid,4);
                    endofTimer(calendar,p_uid,parentid,childid,timestatus);
                }else{
                    Log.v(data,"P_UID pm"+p_uid);
                    //update_app_timer_status(db,p_uid,parentid,childid,4);
                    Log.v(data,"timer pm "+am_pm_c+"  "+oras+":"+mins+" "+format);
                    endofTimer(calendar,p_uid,parentid,childid,timestatus);
                }
            }while (cursor.moveToNext());
        }

        //Toast.makeText(this,"End of parental control ",Toast.LENGTH_SHORT).show();
    }


    public void endofTimer(Calendar calendar,int pid,int parentid,int childid,int timestatus){
        Intent intent = new Intent(this,end_receiver.class);
        intent.setAction(Long.toString(System.currentTimeMillis()));

        intent.putExtra("p_id",pid);
        intent.putExtra("parentid",parentid);
        intent.putExtra("childid",childid);
        intent.putExtra("timestatus",timestatus);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),280192,intent,PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        //Log.v("data","pota end na-------------------->");
    }



}
