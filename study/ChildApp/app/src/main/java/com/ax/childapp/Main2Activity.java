package com.ax.childapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int parentid=12;
    private static int childid;
    final Context context = this;
    private static final int status=1;
    private session_childs session;
    private static final int eventstatus=1;
    private View decorView;
    private Button btn,activate_admin,deactivate_admin;
    Handler handler;
    private PolicyManager policyManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        policyManager = new PolicyManager(this);

        final websiteAdapter dbz = new websiteAdapter(this);

        Intent i = getIntent();

        activate_admin = (Button)findViewById(R.id.activate_admin);
        deactivate_admin = (Button)findViewById(R.id.deactivate_admin);
        //idz
        int idzzz = 0 ;
        final int idzz = i.getIntExtra("id_child",1);

        if (idzz == 0){

            Cursor cursor =  dbz.fetch_session();
            String d = cursor.getString(0);
            idzzz = Integer.parseInt(d);
            Log.v("data","id child titi------------------------------------- "+idzzz);
        } else if (idzz > 0){
            idzzz = idzz;

        }
        final int idz = idzzz;
        Log.v("data","id child titi burat------------------------------------- "+idz);

        //Log.v("data","id child "+idz);

        final int dat_parentid = dbz.getParentId(idz);
        Log.v("data","parentid "+dat_parentid);

        int get_count_session_id = dbz.getCount(dat_parentid,idz);
        if(get_count_session_id!=0){
            Log.v("data","meron id");
        }else{
            Log.v("data","wala");
            long id_countx = dbz.insertSessionID(dat_parentid,idz);
            if(id_countx>0){
                Log.v("data","save successfully ");
            }else{
                Log.v("data","not save");
            }
        }
        Log.v("data","parentid ------------------------------------->"+dat_parentid);

        ImageView logout = (ImageView) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enter Pin to Logout!");
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int idzzz = 0 ;
                        if (idzz == 0){

                            Cursor cursor =  dbz.fetch_session();
                            String d = cursor.getString(0);
                            idzzz = Integer.parseInt(d);
                            Log.v("data","id child titi------------------------------------- "+idzzz);
                        } else if (idzz > 0){
                            idzzz = idzz;
                        }
                        final int idz = idzzz;
                        Log.v("data","id child titi burat------------------------------------- "+idz);
                        //Log.v("data","id child "+idz);
                        final int dat_parentid = dbz.getParentId(idz);

                        Cursor cursor = dbz.getChildAccountPass(idz);
                        String d = cursor.getString(0);
                        Log.v("data","Password = "+d);
                        final String pin = d;
                        if (input.getText().toString().equals(pin)){
                            dbz.DeleteChildAccounts();
                            dbz.delSessionTable(dat_parentid,idz);
                            logout_s();
                        }else {
                            Toast.makeText(Main2Activity.this,"Invalid Pin",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        });

        childid = idz;

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0)
                    decorView.setSystemUiVisibility(hideBar());
            }
        });

        //bullshit permission
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N && !Settings.canDrawOverlays(this)){
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }

        Log.v("data","api version----------------------------------------------------------------------->"+Build.VERSION.SDK_INT);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS}, 1);
            Log.v("data","not granted");
        } else{
            Log.v("data","granted");
        }

        boolean enabled = isAccessibilityServiceEnabled(Main2Activity.this,MyService.class);
        if(enabled==false ){
            AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
            builder.setTitle("Accessibility Event").setMessage("Would you like to to Turn On The Spyrent Kids? ").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(Main2Activity.this,"Find Child App ",Toast.LENGTH_SHORT).show();
                    startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);

                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(Main2Activity.this,"Accessibility event Parental Control is not enable ",Toast.LENGTH_SHORT).show();

                }
            });

            //Creating dialog box
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        //childs table that save to databse
        websiteAdapter web_db = new websiteAdapter(Main2Activity.this);

        Cursor web_cursor = web_db.fetch(childid,status);
        if(web_cursor.moveToFirst()){
            do{
                int uid = web_cursor.getInt(web_cursor.getColumnIndex("_id"));
                String app_name = web_cursor.getString(web_cursor.getColumnIndex("apps_name"));
                //Log.v("data","id that save to childs table "+uid+"-->"+app_name);

            }while (web_cursor.moveToNext());
        }web_cursor.close();

        Cursor web_cursor_nullx = web_db.fetchnulltimer();
        if(web_cursor_nullx.moveToFirst()){
            do {
                String app_name = web_cursor_nullx.getString(web_cursor_nullx.getColumnIndex("apps_name"));
                //Log.v("nulltimer","value---> "+app_name);
            }while (web_cursor_nullx.moveToNext());
        }web_cursor_nullx.close();


        Cursor fetch_all_domain = web_db.fetchdomain();
        if(fetch_all_domain.moveToFirst()){
            do {
                String domain_name = fetch_all_domain.getString(fetch_all_domain.getColumnIndex("domain"));
                int uid = fetch_all_domain.getInt(fetch_all_domain.getColumnIndex("_id"));
                //Log.v("blocklist","domain name----> "+uid+". "+domain_name);
            }while (fetch_all_domain.moveToNext());
        }fetch_all_domain.close();

        //list ng mga app na tumatakbo ang oras para i block
        Cursor cursor_fetch_have_timer = web_db.fetch(childid,3);
        if(cursor_fetch_have_timer.moveToFirst()){
            do {
                String pack_name = cursor_fetch_have_timer.getString(cursor_fetch_have_timer.getColumnIndex("apps_pack"));
                //Log.v("data","pack name of have timer------------------------->"+pack_name);
            }while (cursor_fetch_have_timer.moveToNext());
        }

        //installed apps to the childs (list)

        Cursor cursor_get_all_installed_apps = web_db.fetch_installed_apps_child(12,1);
        if(cursor_get_all_installed_apps.moveToFirst()){
            do {
                String pack_name = cursor_get_all_installed_apps.getString(cursor_get_all_installed_apps.getColumnIndex("apps_pack"));
                String app_name = cursor_get_all_installed_apps.getString(cursor_get_all_installed_apps.getColumnIndex("apps_name"));
                int size = cursor_get_all_installed_apps.getInt(cursor_get_all_installed_apps.getColumnIndex("size"));
                //Log.v("data","app Name installed ------------------------->"+app_name+"------------------------>"+pack_name+"---------------->"+size);
            }while (cursor_get_all_installed_apps.moveToNext());
        }cursor_get_all_installed_apps.close();

        //get all history website
        Cursor cursor_history_site = web_db.fetch_domain_history(12,1);
        if(cursor_history_site.moveToFirst()){
            do {
                String domain = cursor_history_site.getString(cursor_history_site.getColumnIndex("domain"));
                String current_dates = cursor_history_site.getString(cursor_history_site.getColumnIndex("current_dates"));
                //Log.v("data","history domain------------------->"+domain+"------current dates------------------>"+current_dates);
            }while (cursor_history_site.moveToNext());
        }cursor_history_site.close();

        //get all history app
        Cursor cursor_history_app = web_db.fetch_app_history(12,1);
        if(cursor_history_app.moveToFirst()){
            do {
                String app_name = cursor_history_app.getString(cursor_history_app.getColumnIndex("apps_name"));
                String current_dates = cursor_history_app.getString(cursor_history_app.getColumnIndex("current_dates"));
                //Log.v("data","history app------------------->"+app_name+"------current dates------------------>"+current_dates);
            }while (cursor_history_app.moveToNext());
        }cursor_history_app.close();

        session = new session_childs(this);
        if (!session.loggedin()){
            logout_s();
        }


        try {

            //Create a new PendingIntent and add it to the AlarmManager
            Intent intent = new Intent(this, pota.class);
            intent.putExtra("session_idxxx",idz);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am =
                    (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    1*60*60,pendingIntent);

        } catch (Exception e) {}

        try {

            //Create a new PendingIntent and add it to the AlarmManager
            Intent intentx = new Intent(this, timerstart.class);
            //intentx.putExtra("session_idxxx",idz);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    12345, intentx, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am =
                    (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    1*60*60,pendingIntent);

        } catch (Exception e) {}


        try {

            //Create a new PendingIntent and add it to the AlarmManager
            Intent intent = new Intent(this, timerend.class);
            //intent.putExtra("session_idxxx",idz);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am =
                    (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    1*60*60,pendingIntent);

        } catch (Exception e) {}

        if(!policyManager.isAdminActive()){
            Toast.makeText(this,"naka off ang admin",Toast.LENGTH_SHORT).show();
            deactivate_admin.setEnabled(false);
        }else{
            Toast.makeText(this,"naka on ang admin",Toast.LENGTH_SHORT).show();
            activate_admin.setEnabled(false);
        }

    }



    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.activate_admin:
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enter Pin to Activate Admin");
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        websiteAdapter dbz = new websiteAdapter(context);
                        int idzzz = 0 ;
                        int idzz = 0 ;
                        if (idzz == 0){

                            Cursor cursor =  dbz.fetch_session();
                            String d = cursor.getString(0);
                            idzzz = Integer.parseInt(d);
                            Log.v("data","id child titi------------------------------------- "+idzzz);
                        } else if (idzz > 0){
                            idzzz = idzz;

                        }
                        final int idz = idzzz;
                        Log.v("data","id child titi burat------------------------------------- "+idz);
                        //Log.v("data","id child "+idz);
                        final int dat_parentid = dbz.getParentId(idz);

                        Cursor cursor = dbz.getChildAccountPass(idz);
                        String d = cursor.getString(0);
                        Log.v("data","Password = "+d);
                        final String pin = d;
                        if (input.getText().toString().equals(pin)){
                            if(!policyManager.isAdminActive()){
                                Intent activateDeviceAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                                activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,policyManager.getAdminComponent());
                                activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"After activating admin, you will be alble to block application uninstallation");
                                startActivityForResult(activateDeviceAdmin,PolicyManager.DPM_ACTIVATION_REQUEST_CODE);
                                activate_admin.setEnabled(false);
                                deactivate_admin.setEnabled(true);
                            }
                        }else {
                            Toast.makeText(Main2Activity.this,"Invalid Pin",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
                break;
            case R.id.deactivate_admin:
                    AlertDialog.Builder builders = new AlertDialog.Builder(context);
                    builders.setTitle("Enter Pin to Deactivate Admin");
                    final EditText inputs = new EditText(context);
                    inputs.setInputType(InputType.TYPE_CLASS_TEXT);
                    inputs.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    builders.setView(inputs);
                    builders.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            websiteAdapter dbz = new websiteAdapter(context);
                            int idzzz = 0 ;
                            int idzz = 0 ;
                            if (idzz == 0){

                                Cursor cursor =  dbz.fetch_session();
                                String d = cursor.getString(0);
                                idzzz = Integer.parseInt(d);
                                Log.v("data","id child titi------------------------------------- "+idzzz);
                            } else if (idzz > 0){
                                idzzz = idzz;

                            }
                            final int idz = idzzz;
                            Log.v("data","id child titi burat------------------------------------- "+idz);
                            //Log.v("data","id child "+idz);
                            final int dat_parentid = dbz.getParentId(idz);

                            Cursor cursor = dbz.getChildAccountPass(idz);
                            String d = cursor.getString(0);
                            Log.v("data","Password = "+d);
                            final String pin = d;
                            if (inputs.getText().toString().equals(pin)) {
                                policyManager.disableAdmin();
                                activate_admin.setEnabled(true);
                                deactivate_admin.setEnabled(false);

                            }else {
                                Toast.makeText(Main2Activity.this,"Invalid Pin",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builders.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builders.show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == PolicyManager.DPM_ACTIVATION_REQUEST_CODE){
            Log.v("successfully"," enabled the admin");
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
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
                        //Log.v("data","installed apps insert response ------------------->"+response);
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

    //null timer app
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

                        //Log.v("data","apps_pack--------->"+data.getString("apps_pack"));

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

    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            decorView.setSystemUiVisibility(hideBar());
        }
    }


    private int hideBar(){
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    private void logout_s(){
        session.setLoggedin(false);
        finish();
        startActivity(new Intent(this,MainActivity.class));
    }

    //check Internet Connections

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



    public void exit1(View v) {
        // TODO Auto-generated method stub
        finish();
        System.exit(0);
    }

}
