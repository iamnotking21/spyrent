package com.ax.childapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Map;

public class checkInternetconnections extends BroadcastReceiver {

    @Override

    public void onReceive(final Context context, Intent intent) {

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){

            fun_lang(context,intent);

            //Log.v("data","boot completed");

        }else{

            //Toast.makeText(context,"not boot completed",Toast.LENGTH_SHORT).show();

            fun_lang(context,intent);

        }

        /*

        try {



            //Create a new PendingIntent and add it to the AlarmManager

            intent = new Intent(context, pota1.class);

            intent.putExtra("session_idxxx",child_id_ses);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,

                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager am =

                    (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),

                    1*60*60,pendingIntent);



        } catch (Exception e) {}

        */



        //context.startService(new Intent(context,MyService.class));





        /*

        boolean connected = checkInternet(context);



        Log.v("data","internet child--------------------> "+connected);

        if(connected==true){

            db.DeleteChildAccounts();



            for(int i = 0 ; i < child_id_ses.size(); i++){



                //deleted_installed_apps_online_server(context,child_id_ses.get(i));



                db.delete_null_timer(child_id_ses.get(i));

                db.delete_domain_block_list(child_id_ses.get(i));

                db.delete_block_app_have_timer(child_id_ses.get(i));



                insertNullTimer(context,child_id_ses.get(i));

                insertdomain(context,child_id_ses.get(i));

                insertHaveTimer(context,child_id_ses.get(i));



                insertHistory_app(context,child_id_ses.get(i));

                insertHistory_site(context,child_id_ses.get(i));

                */



                /*

                insertNullTimer(context,child_id_ses.get(i));

                insertHistory_app(context,child_id_ses.get(i));

                insertHistory_site(context,child_id_ses.get(i));

                insertHaveTimer(context,child_id_ses.get(i));

                insertdomain(context,child_id_ses.get(i));

                */





                /*

                int countNullTimer = db.countNullTimer(child_id_ses.get(i));

                if(countNullTimer == 0 ){

                    Log.v("data","walang laman null timer ----------------------------------->"+countNullTimer);

                    insertNullTimer(context,child_id_ses.get(i));

                }



                int countBlockList = db.CountblockList(child_id_ses.get(i));

                if(countBlockList == 0 ){

                    Log.v("data","walang laman block list ------------------------------------->"+countBlockList);

                    insertdomain(context,child_id_ses.get(i));

                }



                int countHavaeTimer = db.CounthaveTimer(child_id_ses.get(i));

                if(countHavaeTimer == 0){

                    Log.v("data","walang laman have timer ------------------------------------->"+countHavaeTimer);

                    insertHaveTimer(context,child_id_ses.get(i));

                }



            }





            Toast.makeText(context,"meron mobile data child data",Toast.LENGTH_SHORT).show();

        }else{

            Toast.makeText(context,"wala mobile data child data",Toast.LENGTH_SHORT).show();

        }



        CountDownTimer c = new CountDownTimer(3000,1000){

            @Override

            public void onTick(long millisUntilFinished) {

                Log.v("data","remaining time ------------->"+millisUntilFinished);

            }



            @Override

            public void onFinish() {

                int childCount = db.getChildAccountCount();

                if(childCount == 0 ){

                    insert_account_child(context);

                }



                for(int b = 0 ; b < parentid_ses.size(); b++){

                    insertmobileParent(context,parentid_ses.get(b));

                }





            }

        }.start();



        */



        /*

        for(int fg = 0 ; fg < child_id_ses.size(); fg++){

            // insertInsertInstalledApps(context,child_id_ses.get(fg));

        }

        */



        //check if the Accessibility Event is on/off

        /*

        if(isAccessibilityServiceEnabled(context,MyService.class)==true){

            Log.v("data","naka on");

        }else{



            Log.v("data","naka off");



            if(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED) {

                Log.v("data","not granted");

            } else{

                Log.v("data","granted");



                for(int m = 0 ; m < parentid_ses.size(); m++){

                    Cursor data_mobile = db.getmobile_parent(parentid_ses.get(m));



                    if(data_mobile.moveToFirst()){

                        do {

                            String numero = data_mobile.getString(data_mobile.getColumnIndex("number"));

                            sendSMS(numero,"The accessibility Event is OFF please turn on");

                        }while (data_mobile.moveToNext());

                    }data_mobile.close();



                }





            }

        }

        */





    }

    public void fun_lang(Context context,Intent intent){

        final websiteAdapter db = new websiteAdapter(context);



        final ArrayList<Integer> child_id_ses = getSessionId(context);

        final ArrayList<Integer> parentid_ses = getSessionParentid(context);



        Log.v("data","child id -------------------xxxxx "+child_id_ses+" parentid "+parentid_ses);

        //Toast.makeText(context,"putangina",Toast.LENGTH_SHORT).show();

        try {



            //Create a new PendingIntent and add it to the AlarmManager

            intent = new Intent(context, pota.class);

            intent.putExtra("session_idxxx",child_id_ses);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,

                    45736, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager am =

                    (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),

                    1*60*60,pendingIntent);



        } catch (Exception e) {}



        try {



            //Create a new PendingIntent and add it to the AlarmManager

            Intent intentx = new Intent(context, timerstart.class);

            //intentx.putExtra("session_idxxx",idz);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,

                    12345, intentx, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager am =

                    (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);

            am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),

                    1*60*60,pendingIntent);



        } catch (Exception e) {}





        try {



            //Create a new PendingIntent and add it to the AlarmManager

            Intent intenta = new Intent(context, timerend.class);

            //intent.putExtra("session_idxxx",idz);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,

                    12345, intenta, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager am =

                    (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);

            am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),

                    1*60*60,pendingIntent);



        } catch (Exception e) {}



    }
    public void sendSMS(String number,String mes){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number,null,mes,null,null);
    }

    public void insertmobileParent(Context context,final int parentid ){
        final websiteAdapter db = new websiteAdapter(context);

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrent.online/res_api/select_all_number_parent.php?id="+parentid+" ";


        StringRequest stringRequest =  new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response mobile number"+response);
                try{
                    JSONArray array = new JSONArray(response);

                    for(int i = 0; i < array.length(); i++){

                        JSONObject data = array.getJSONObject(i);

                        data.getInt("ID");
                        data.getString("mobile_number");

                        Log.v("data","parent mobile number------------------> "+data.getString("mobile_number"));

                        int count_mobile = db.getCountMobile(data.getString("mobile_number"),data.getInt("ID"));
                        if(count_mobile!=0){
                            Log.v("data","meron number ");
                        }else{
                            Log.v("data","wala number ");

                            long id = db.insertParentNumber(data.getString("mobile_number"),data.getInt("ID"));
                            if(id!=0){
                                Log.v("data","save successfully number -------------------------------------------->");
                            }else{

                                Log.v("data","not  number -------------------------------------------->");
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
                Log.v("data","error mobile number-----------------> "+error);
            }
        });
        queue.add(stringRequest);

    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }

        return false;
    }

    public void deleted_installed_apps_online_server(Context context, final int primary_key){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrent.online/res_api/del_installed_apps.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","deleted installed apps-------------> "+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error installed apps "+error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("id",String.valueOf(primary_key));

                return params;
            }
        };
        queue.add(stringRequest);

    }

    public void insert_account_child(Context context){
        RequestQueue queue = Volley.newRequestQueue(context);

        final websiteAdapter db = new websiteAdapter(context);

        String url = "http://spyrent.online/res_api/select_all_child_account.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray array = new JSONArray(response);

                    for(int i = 0 ; i < array.length(); i++){

                        JSONObject data = array.getJSONObject(i);

                        data.getInt("ID");
                        data.getString("CNAME");
                        data.getString("CPASS");
                        data.getInt("status_child");
                        data.getInt("parentid");

                        int id_count = db.CountIDChild(data.getInt("ID"));
                        if(id_count != 0){
                            Log.v("data","meron ng primary key ito-------------------->");
                        }else{
                            Log.v("data","wala pa primary key ito--------------------->");

                            //insert
                            long id_val = db.insertChildAccount(data.getInt("ID"),data.getString("CNAME"),data.getString("CPASS"),data.getInt("status_child"),data.getInt("parentid"));
                            if(id_val > 0){
                                Log.v("data","save successfully child account ");
                            }else{
                                Log.v("data","not save successfully child account");
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
                Log.v("data","error response child account-------------------------------> "+error);
            }
        });
        queue.add(stringRequest);
    }

    public ArrayList<Integer> getSessionId(Context context){
        websiteAdapter db = new websiteAdapter(context);

        ArrayList<Integer> data_child = db.getchildIdsession();
        //Log.v("data","session id child ------------------>"+data_child);
        return data_child;
    }

    public ArrayList<Integer> getSessionParentid(Context context){
        websiteAdapter db = new websiteAdapter(context);

        ArrayList<Integer> data_parent = db.getparentIDsession();

        return data_parent;

    }

    public void insertNullTimer(Context context,final int id){
        RequestQueue queue = Volley.newRequestQueue(context);

        final websiteAdapter db = new websiteAdapter(context);

        String url = "http://spyrent.online/res_api/select_all_null_timer_app.php?id="+id+" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);

                    for(int i = 0 ; i < array.length(); i++){
                        JSONObject data = array.getJSONObject(i);

                        data.getInt("_id");
                        data.getInt("loginIdparent");
                        data.getInt("loginIdchild");
                        data.getString("apps_pack");
                        data.getString("apps_name");
                        data.getInt("eventstatus");

                        Log.v("data","apps_pack--------->"+data.getString("apps_pack"));

                        int null_timer_count = db.getidnulltimer(data.getInt("loginIdparent"),data.getInt("loginIdchild"),data.getString("apps_name"));
                        if(null_timer_count!=0){
                            Log.v("null timer---->","meron--->"+data.getString("apps_name"));
                        }else{

                            //save to database
                            long id_save_null_timer_active = db.insertnulltimer(data.getInt("loginIdparent"),data.getInt("loginIdchild"),data.getString("apps_name"),data.getString("apps_pack"));
                            if(id_save_null_timer_active!=0){
                                Log.v("save","successfully null timer");
                            }else{
                                Log.v("not save","null timer");
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
                Log.v("data","error response null timer app --------------->"+error);
            }
        });
        queue.add(stringRequest);
    }

    public void insertHistory_site(Context context,final int primary_id){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrent.online/res_api/post_history_site.php";

        websiteAdapter web_db = new websiteAdapter(context);

        Cursor cursor_history_site = web_db.fetch_domain_history(12,1);
        if(cursor_history_site.moveToFirst()){
            do {
                final String domain = cursor_history_site.getString(cursor_history_site.getColumnIndex("domain"));
                final String current_dates = cursor_history_site.getString(cursor_history_site.getColumnIndex("current_dates"));
                //Log.v("data","history domain------------------->"+domain+"------current dates------------------>"+current_dates);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.v("data","response history site------------------------------------------"+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.v("data","error site-----------------------------------------------------"+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("domain",domain);
                        params.put("current_dates",current_dates);
                        params.put("id",String.valueOf(primary_id));

                        return params;
                    }
                };
                queue.add(stringRequest);

            }while (cursor_history_site.moveToNext());
        }cursor_history_site.close();


    }

    public void insertHistory_app(Context context,final int primary_id){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrent.online/res_api/post_history_apps.php";

        websiteAdapter web_db = new websiteAdapter(context);

        Cursor cursor_history_app = web_db.fetch_app_history(12,1);
        if(cursor_history_app.moveToFirst()){
            do {
                final String app_namex = cursor_history_app.getString(cursor_history_app.getColumnIndex("apps_name"));
                final String current_dates = cursor_history_app.getString(cursor_history_app.getColumnIndex("current_dates"));
                //Log.v("data","history app------------------->"+app_name+"------current dates------------------>"+current_dates);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.v("Data","response history app-------------------------------------------> "+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.v("Data","error history app-------------------------------------------> "+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("app_name",app_namex);
                        params.put("current_dates",current_dates);
                        params.put("id",String.valueOf(primary_id));

                        return params;
                    }
                };
                queue.add(stringRequest);

            }while (cursor_history_app.moveToNext());
        }cursor_history_app.close();

    }

    public void insertInsertInstalledApps(Context context,final int  primary_id){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrent.online/res_api/post_installed_apps.php";

        websiteAdapter web_db = new websiteAdapter(context);
        Cursor cursor_get_all_installed_apps = web_db.fetch_installed_apps_child(12,1);
        if(cursor_get_all_installed_apps.moveToFirst()){
            do {
                final String pack_namex = cursor_get_all_installed_apps.getString(cursor_get_all_installed_apps.getColumnIndex("apps_pack"));
                final String app_namex = cursor_get_all_installed_apps.getString(cursor_get_all_installed_apps.getColumnIndex("apps_name"));
                final int sizex = cursor_get_all_installed_apps.getInt(cursor_get_all_installed_apps.getColumnIndex("size"));
                //Log.v("data","app Name installed ------------------------->"+app_name+"------------------------>"+pack_name+"---------------->"+size);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("data","installed apps insert response ------------------->"+response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.v("data","error response post installed apps---------------->"+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("pack_name",pack_namex);
                        params.put("app_name",app_namex);
                        params.put("size",String.valueOf(sizex));
                        params.put("id",String.valueOf(primary_id));

                        return params;
                    }
                };
                queue.add(stringRequest);


            }while (cursor_get_all_installed_apps.moveToNext());
        }cursor_get_all_installed_apps.close();

    }
    public void insertHaveTimer(Context context,int id){
        RequestQueue queue = Volley.newRequestQueue(context);

        final websiteAdapter db =  new websiteAdapter(context);

        String url = "http://spyrent.online/res_api/select_all_have_timer.php?id="+id+" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);

                    for(int i = 0 ; i < array.length(); i++){
                        JSONObject data = array.getJSONObject(i);

                        data.getInt("_id");
                        data.getInt("status_child");
                        data.getInt("eventstatus");
                        data.getInt("loginIdparent");
                        data.getInt("loginIdchild");

                        data.getString("apps_name");
                        data.getString("apps_pack");

                        data.getInt("oras");
                        data.getInt("mins");
                        data.getInt("timeset");

                        data.getInt("soras");
                        data.getInt("smins");
                        data.getInt("stimeset");


                        int id_count = db.getidsame(data.getInt("loginIdparent"),data.getInt("loginIdchild"),data.getInt("status_child"),data.getString("apps_name"));
                        if(id_count!=0){
                            //Log.v("data","id count merong laman "+data.get(i));
                        }else{
                            //Log.v("data","id count walang laman");

                            //save to database
                            long id = db.insertData1(data.getString("apps_pack"),data.getString("apps_name"),data.getInt("oras"),data.getInt("mins"),data.getInt("timeset"),data.getInt("soras"),data.getInt("smins"),data.getInt("stimeset"),data.getInt("loginIdparent"),data.getInt("loginIdchild"),data.getInt("eventstatus"));
                            if(id>=0){
                                Log.v("save","successfully");
                            }else{
                                Log.v("not"," not save");
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
                Log.v("data","error have timer ------------------xxxxxxxxxx"+error);
            }
        });
        queue.add(stringRequest);

    }
    public void insertdomain(Context context,final int id){
        RequestQueue queue = Volley.newRequestQueue(context);

        final websiteAdapter db = new websiteAdapter(context);

        String url = "http://spyrent.online/res_api/select_all_domain.php?id="+id+" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);

                    for(int i = 0 ; i < array.length(); i++){

                        JSONObject data = array.getJSONObject(i);

                        data.getInt("_id");
                        data.getString("domain");
                        data.getInt("status_child");
                        data.getInt("loginIdparent");
                        data.getInt("loginIdchild");

                        //Log.v("data","domain-------------->"+data.getString("domain"));

                        int id_count = db.getiddomain(data.getInt("loginIdparent"),data.getInt("loginIdchild"),data.getString("domain"));
                        if(id_count!=0){
                            //Log.v("domain","may laman "+domain_data.get(x));
                        }else{
                            //Log.v("domain","walang laman "+domain_data.get(x));
                            long idx_counter = db.insertDomainBlocklist(data.getInt("loginIdparent"),data.getInt("loginIdchild"),data.getString("domain"),data.getInt("status_child"));
                            if(idx_counter!=0){
                                //Log.v("save","successfully domain");
                            }else{
                                //Log.v("not save"," not not ");
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
                Log.v("data","error response domain----------->"+error);
            }
        });
        queue.add(stringRequest);

    }

    //check internet function
    public boolean checkInternet(Context context){
        boolean val ;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ) {
            Toast.makeText(context,"Updated account child meorng internet",Toast.LENGTH_SHORT).show();
            val = true;
        }else{
            Toast.makeText(context,"Updated account child walang internet",Toast.LENGTH_SHORT).show();
            val = false;
        }
        return val;

    }


}
