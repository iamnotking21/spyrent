package com.ax.childapp;

import android.content.Context;
import android.content.SharedPreferences;

public class session_childs {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Context ctx;



    public session_childs(Context ctx){
        this.ctx = ctx;
        prefs = ctx.getSharedPreferences("pota3", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void setLoggedin(boolean login){
        editor.putBoolean("LoggedInmode", login);
        editor.commit();
    }

    public boolean loggedin(){
        return prefs.getBoolean("LoggedInmode", false);
    }







}
