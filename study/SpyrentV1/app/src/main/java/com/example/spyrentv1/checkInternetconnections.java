package com.example.spyrentv1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class checkInternetconnections extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {
        /*if(isOnline(context)){
            Log.v("data","may data");
            Toast.makeText(context,"wifi data",Toast.LENGTH_SHORT).show();
            fetchAllUsernameParent(context);
        }
        else{
            Log.v("data","wala data");
            Toast.makeText(context,"wala data",Toast.LENGTH_SHORT).show();
        }*/




        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;
        Log.v("data","internet--------------------> "+connected);
        if(connected==true){
            del_all_data_according_to_parentid(context);

            del_all_webTable(context);
            del_all_walangtimer(context);
            del_all_have_timer(context);


            fetchAllUsernameParent(context);

            Toast.makeText(context,"meron mobile data",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"wala mobile data",Toast.LENGTH_SHORT).show();
        }

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.v("data","remaining "+millisUntilFinished);
            }

            public void onFinish() {
                fetch_have_timer_apps(context);
                fetchAllChild(context);
                fetch_all_web(context);
                fetch_walang_timer(context);
            }

        }.start();

    }

    public boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    public void updateAccount(Context context){
        DBManager db = new DBManager(context);
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/updateUserChild.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response update ok -------------------------->"+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error update ------------------------------>"+error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();

                return params;
            }
        };
        queue.add(stringRequest);

    }

    public void fetchAllUsernameParent(Context context){
        DatabaseHelper db = new DatabaseHelper(context);

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/post.php";


        Cursor cursor = db.getAllUname();
        if(cursor.moveToFirst()){
            do {

                final String firstname = cursor.getString(cursor.getColumnIndex("FNAME"));
                final String lastname = cursor.getString(cursor.getColumnIndex("LNAME"));
                final String email = cursor.getString(cursor.getColumnIndex("EMAIL"));
                final String uname = cursor.getString(cursor.getColumnIndex("UNAME"));
                final String password = cursor.getString(cursor.getColumnIndex("PASSWORD"));
                final String pos = cursor.getString(cursor.getColumnIndex("POS"));


                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("save")) {
                            Log.v("data", "ok "+uname);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error "+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("firstname",firstname);
                        params.put("lastname",lastname);
                        params.put("email",email);
                        params.put("username",uname);
                        params.put("password",password);
                        params.put("pos",pos);

                        return params;
                    }
                };

                queue.add(stringRequest);

            }while (cursor.moveToNext());
        }cursor.close();
    }

    public void fetchAllChild(Context context){
        DatabaseHelper db = new DatabaseHelper(context);

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/post_child_register.php";

        Cursor cursor = db.getAllChildAccounts(1);

        if(cursor.moveToFirst()){
            do {
                final String cname = cursor.getString(cursor.getColumnIndex("CNAME"));
               // final String uname = cursor.getString(cursor.getColumnIndex("UNAME"));
                final String cpass = cursor.getString(cursor.getColumnIndex("CPASS"));
                final String status = cursor.getString(cursor.getColumnIndex("status_child"));
                final int sqlite_id = cursor.getInt(cursor.getColumnIndex("ID"));

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("data","response------------------xxxx "+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error child register "+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("cname",cname);
                        params.put("cpass",cpass);
                       // params.put("uname",uname);
                        params.put("status",status);
                        params.put("sqlite_id",String.valueOf(sqlite_id));

                        return params;
                    }
                };
                queue.add(stringRequest);
            }while (cursor.moveToNext());
        }cursor.close();

    }

    public void fetch_all_web(Context context){
        DatabaseHelper db = new DatabaseHelper(context);

        RequestQueue queue = Volley.newRequestQueue(context);

        final String url = "http://spyrents.xyz/res_api/postWebsite.php";

        Cursor cursor = db.getAllwebTable(1);

        if(cursor.moveToFirst()){
            do {
                final String domainx =  cursor.getString(cursor.getColumnIndex("domain"));
                final String status_child =  cursor.getString(cursor.getColumnIndex("status_child"));

                final int parentidx = cursor.getInt(cursor.getColumnIndex("loginIdparent"));
                final int childidx =  cursor.getInt(cursor.getColumnIndex("loginIdchild"));

                final String parentId = db.parent_name(parentidx);
                final String childId = db.child_name(childidx);

                Log.v("dataa","parent name --------------->"+parentId);
                Log.v("dataa","child name ---------------->"+childId);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("data","response website----------------------->"+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error "+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("parent_name",parentId);
                        params.put("child_name",childId);
                        params.put("domain",domainx);
                        params.put("status_child",String.valueOf(status_child));
                        return params;
                    }

                };
                queue.add(stringRequest);
            }while (cursor.moveToNext());
        }cursor.close();
    }

    public void fetch_have_timer_apps(Context context){
        DatabaseHelper db = new DatabaseHelper(context);
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://spyrents.xyz/res_api/postHaveTimerApp.php";

        Cursor cursor = db.getAllHaveTimerApps(1);
        if(cursor.moveToFirst()){
            do {
                int parentid = cursor.getInt(cursor.getColumnIndex("loginIdparent"));
                int childid = cursor.getInt(cursor.getColumnIndex("loginIdchild"));

                final String parent_name = db.parent_name(parentid);
                final String child_name = db.child_name(childid);

                final String app_namex = cursor.getString(cursor.getColumnIndex("apps_name"));
                final String pack_namex = cursor.getString(cursor.getColumnIndex("apps_pack"));

                final int orasx = cursor.getInt(cursor.getColumnIndex("oras"));
                final int minsx = cursor.getInt(cursor.getColumnIndex("mins"));
                final int timesetx = cursor.getInt(cursor.getColumnIndex("timeset"));

                final int sorasx = cursor.getInt(cursor.getColumnIndex("soras"));
                final int sminsx = cursor.getInt(cursor.getColumnIndex("smins"));
                final int stimesetx = cursor.getInt(cursor.getColumnIndex("stimeset"));

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("data","response all app have timer ------------>"+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error all app have timer--------------------> "+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();

                        params.put("parentname",parent_name);
                        params.put("childname",child_name);

                        params.put("app_name",app_namex);
                        params.put("pack_name",pack_namex);

                        params.put("oras",String.valueOf(orasx));
                        params.put("mins",String.valueOf(minsx));
                        params.put("timeset",String.valueOf(timesetx));

                        params.put("soras",String.valueOf(sorasx));
                        params.put("smins",String.valueOf(sminsx));
                        params.put("stimeset",String.valueOf(stimesetx));

                        return params;
                    }
                };
                queue.add(stringRequest);
            }while (cursor.moveToNext());
        }cursor.close();
    }

    public void fetch_walang_timer(Context contextx){
        DatabaseHelper db = new DatabaseHelper(contextx);

        RequestQueue queue = Volley.newRequestQueue(contextx);

        String url = "http://spyrents.xyz/res_api/postnullTimer.php";

        Cursor cursor = db.getAllWalangTimer(1);
        if(cursor.moveToFirst()){
            do {
                int parentid = cursor.getInt(cursor.getColumnIndex("loginIdparent"));
                int childid = cursor.getInt(cursor.getColumnIndex("loginIdchild"));

                final String apps_name = cursor.getString(cursor.getColumnIndex("apps_name"));
                final String apps_pack = cursor.getString(cursor.getColumnIndex("apps_pack"));

                final int eventStatus = cursor.getInt(cursor.getColumnIndex("eventstatus"));

                final String parentIds = db.parent_name(parentid);
                final String childIds = db.child_name(childid);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("data","response --------walang timer--------"+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error-------walang timer --------->"+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();

                        params.put("parentname",parentIds);
                        params.put("childname",childIds);
                        params.put("packname",apps_pack);
                        params.put("appname",apps_name);
                        params.put("eventstatus",String.valueOf(eventStatus));

                        return params;
                    }
                };
                queue.add(stringRequest);


            }while (cursor.moveToNext());
        }cursor.close();
    }


    public void del_all_have_timer(Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        DatabaseHelper db = new DatabaseHelper(context);
        String url = "http://spyrents.xyz/res_api/del_have_timer.php";

        Cursor cursor = db.getAllUname();
        if(cursor.moveToFirst()){
            do {
                final String uname = cursor.getString(cursor.getColumnIndex("UNAME"));
                final String password = cursor.getString(cursor.getColumnIndex("PASSWORD"));

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("data", "ok---------------> "+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error "+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();

                        params.put("username",uname);
                        params.put("password",password);

                        return params;
                    }
                };

                queue.add(stringRequest);

            }while (cursor.moveToNext());
        }cursor.close();
    }

    public void del_all_walangtimer(Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://spyrents.xyz/res_api/del_post_walang_timer.php";

        DatabaseHelper db = new DatabaseHelper(context);

        Cursor cursor = db.getAllUname();
        if(cursor.moveToFirst()){
            do {
                final String uname = cursor.getString(cursor.getColumnIndex("UNAME"));
                final String password = cursor.getString(cursor.getColumnIndex("PASSWORD"));

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("data", "ok---------------> "+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error "+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();

                        params.put("username",uname);
                        params.put("password",password);

                        return params;
                    }
                };

                queue.add(stringRequest);

            }while (cursor.moveToNext());
        }cursor.close();

    }

    public void del_all_webTable(Context context){
        RequestQueue queue = Volley.newRequestQueue(context);

        DatabaseHelper db = new DatabaseHelper(context);

        String url = "http://spyrents.xyz/res_api/del_post_webTable.php";

        Cursor cursor = db.getAllUname();
        if(cursor.moveToFirst()){
            do {
                final String uname = cursor.getString(cursor.getColumnIndex("UNAME"));
                final String password = cursor.getString(cursor.getColumnIndex("PASSWORD"));

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("data", "ok---------------> "+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error "+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();

                        params.put("username",uname);
                        params.put("password",password);

                        return params;
                    }
                };

                queue.add(stringRequest);

            }while (cursor.moveToNext());
        }cursor.close();
    }

    public void del_all_data_according_to_parentid(Context context){

        DatabaseHelper db = new DatabaseHelper(context);

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://spyrents.xyz/res_api/del_parentid.php";

        Cursor cursor = db.getAllChildAccounts(1);

        if(cursor.moveToFirst()){
            do {

                final String cname = cursor.getString(cursor.getColumnIndex("CNAME"));
                final String cpass = cursor.getString(cursor.getColumnIndex("CPASS"));

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("data","del parentid --------------->"+response);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error parentid "+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("cname",cname);
                        params.put("cpass",cpass);
                        return params;
                    }
                };
                queue.add(stringRequest);


            }while (cursor.moveToNext());
        }cursor.close();

    }

    public boolean check_mobile_data(Context context){
        boolean mobileDataEnabled=false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean)method.invoke(cm);
            Log.v("data","mobile data "+mobileDataEnabled);

        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        return mobileDataEnabled;
    }





}
