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

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class pota1 extends BroadcastReceiver {
    public static final String data = "start timer";
    private int parentid=12,childid=1,status=1,am_pm_c,soras,smins,sformat,p_uid;
    public Calendar calendar;

    public static final String data_e = "End timer";
    private int parentid_e=12,childid_e=1,status_e=1,am_pm_c_e,oras_e,mins_e,format_e,p_uid_e,timestatus_e;
    public Calendar calendar_e;

    @SuppressLint("WrongConstant")
    @Override
    public void onReceive(Context context, Intent intent) {
    }


}
