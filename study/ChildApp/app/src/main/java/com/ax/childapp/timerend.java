package com.ax.childapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import static android.content.Context.ALARM_SERVICE;
import java.util.Calendar;
import java.util.Random;

public class timerend extends BroadcastReceiver {
    public static final String data = "End timer";
    private int parentid=12,childid=1,status=1,am_pm_c,oras,mins,format,p_uid,loginIdparent,loginIdchild;
    private String app_name;
    public Calendar calendar;
    @SuppressLint("WrongConstant")
    @Override
    public void onReceive(Context context, Intent intent) {

        websiteAdapter db = new websiteAdapter(context);

        Cursor cursor = db.fetchend(1);
        if(cursor.moveToFirst()){
            do{
                p_uid = cursor.getInt(cursor.getColumnIndex("p_id"));
                oras = cursor.getInt(cursor.getColumnIndex("oras"));
                mins = cursor.getInt(cursor.getColumnIndex("mins"));
                format = cursor.getInt(cursor.getColumnIndex("timeset"));
                app_name = cursor.getString(cursor.getColumnIndex("apps_name"));

                loginIdparent = cursor.getInt(cursor.getColumnIndex("loginIdparent"));
                loginIdchild = cursor.getInt(cursor.getColumnIndex("loginIdchild"));

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
                    endofTimer(calendar,p_uid,parentid,childid,context,app_name,loginIdparent,loginIdchild);
                }else{
                    Log.v(data,"P_UID pm"+p_uid);
                    //update_app_timer_status(db,p_uid,parentid,childid,4);
                    Log.v(data,"timer pm "+am_pm_c+"  "+oras+":"+mins+" "+format);
                    endofTimer(calendar,p_uid,parentid,childid,context,app_name,loginIdparent,loginIdchild);
                }
            }while (cursor.moveToNext());
        }
    }

    public void endofTimer(Calendar calendar,int pid,int parentid,int childid,Context context,String app_name,int loginIdparent,int loginIdchild){
        Intent intent = new Intent(context,end_receiver.class);
        intent.setAction(Long.toString(System.currentTimeMillis()));

        intent.putExtra("p_id",pid);
        intent.putExtra("parentid",parentid);
        intent.putExtra("childid",childid);
        intent.putExtra("app_name",app_name);

        intent.putExtra("loginIdparent",loginIdparent);
        intent.putExtra("loginIdchild",loginIdchild);

        int dummyuniqueInt = new Random().nextInt(543254);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),dummyuniqueInt,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        //Log.v("data","pota end na-------------------->");
    }

}
