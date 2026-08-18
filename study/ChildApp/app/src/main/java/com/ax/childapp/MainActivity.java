package com.ax.childapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private View decorView;
    checkInternetconnections checkInternetconnections;
    private Button btn;

    Handler handler;
    private TextInputLayout username,password;
    private session_childs session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new session_childs(this);
        final websiteAdapter db = new websiteAdapter(MainActivity.this);
         this.handler = new Handler();
         this.handler.postDelayed(runnable,5000);

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0)
                    decorView.setSystemUiVisibility(hideBar());
            }
        });

        username = (TextInputLayout) findViewById(R.id.textInputLayout);
        password = (TextInputLayout) findViewById(R.id.textInputLayout2);


        if (session.loggedin()){
            int username_id = 0;
            Intent intent = new Intent(this, Main2Activity.class);

            intent.putExtra("id_child",username_id);

            startActivity(intent);
            finish();

        }

        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = db.logIdChild(username.getEditText().getText().toString().trim(),password.getEditText().getText().toString().trim());
                if(count != 0 ){
                    session.setLoggedin(true);
                    int id_child = db.getIdChild(username.getEditText().getText().toString(),password.getEditText().getText().toString());
                    panel(id_child);

                }else{
                    Toast.makeText(MainActivity.this,"Wrong username or password ",Toast.LENGTH_SHORT).show();
                }

            }
        });

        int childCount = db.getChildAccountCount();
        if(childCount != 0 ){
            Toast.makeText(this,"Meron ng laman ",Toast.LENGTH_SHORT).show();

        }else if(childCount == 0){
            boolean val = checkInternet(MainActivity.this);
            if(val==true){
                Toast.makeText(this,"Connected updated",Toast.LENGTH_SHORT).show();

                //updatind account

                db.DeleteChildAccounts();

                int childCountx = db.getChildAccountCount();
                if(childCountx == 0 ){
                    insert_account_child(this);
                }

            }else{
                Toast.makeText(this,"Not Connected ",Toast.LENGTH_SHORT).show();
            }
        }
        //check internet connections
    }


    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            decorView.setSystemUiVisibility(hideBar());
        }
    }


    private final Runnable runnable = new Runnable() {
        @Override                                                      
        public void run() {
            checkInternetconnections checkInternetconnections = new checkInternetconnections();

           //Toast.makeText(MainActivity.this,"Wrong username or password ",Toast.LENGTH_SHORT).show();
            MainActivity.this.handler.postDelayed(runnable,5000);
        }
    };

    private int hideBar(){
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    public void panel(int username_id){
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putExtra("id_child",username_id);
        startActivity(intent);
    }

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

    //insert account child
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
}
