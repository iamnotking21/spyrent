package com.ax.childapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class doDaily extends Service {
    public static final String data = "start timer";
    private int parentid=12,childid=1,status=1,am_pm_c,soras,smins,sformat,p_uid;
    public Calendar calendar;
    public doDaily() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startalert();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;}


    @SuppressLint("WrongConstant")

    public void startalert(){
        Toast.makeText(this,"start of parental control ", Toast.LENGTH_LONG).show();

        //gumawa ng return value para sa array list

        websiteAdapter db = new websiteAdapter(doDaily.this);



        Cursor cursor = db.fetch(1);

        if (cursor.moveToFirst()){

            do{

                p_uid = cursor.getInt(cursor.getColumnIndex("p_id"));

                soras = cursor.getInt(cursor.getColumnIndex("soras"));

                smins = cursor.getInt(cursor.getColumnIndex("smins"));

                sformat = cursor.getInt(cursor.getColumnIndex("stimeset"));



                Log.v(data,"vallll timer "+soras+":"+smins+" "+sformat);



                calendar = Calendar.getInstance();

                calendar.setTimeInMillis(System.currentTimeMillis());

                calendar.set(Calendar.HOUR,soras);

                calendar.set(Calendar.MINUTE,smins);

                //kailangan i set kung am/pm through database

                calendar.set(Calendar.AM_PM,sformat);

                am_pm_c = calendar.get(Calendar.AM_PM);



                Log.v(data,"P_UID------------->"+p_uid);



                if(am_pm_c!=calendar.get(Calendar.AM)){

                    Log.v(data,"P_UID "+p_uid);

                    Log.v(data,"timer am "+am_pm_c+" "+soras+":"+smins+" "+sformat);

                    //update_app_timer_status(db,p_uid,parentid,childid,3);



                    startOfTimer(calendar,p_uid);

                }else{



                    Log.v(data,"P_UID "+p_uid);

                    //update_app_timer_status(db,p_uid,parentid,childid,3);

                    startOfTimer(calendar,p_uid);

                    Log.v(data,"timer pm "+am_pm_c+" "+soras+":"+smins+" "+sformat);

                }

            }while(cursor.moveToNext());

        }

        cursor.close();





    }



    public void startOfTimer(Calendar calendar,int p_id){

        Intent intent = new Intent(this,dodaily_reciever.class);

        intent.setAction(Long.toString(System.currentTimeMillis()));



        intent.putExtra("p_id",p_id);

        Log.v("data","p_id "+p_id);



        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),82737,intent,PendingIntent.FLAG_ONE_SHOT);



        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);



    }


}
