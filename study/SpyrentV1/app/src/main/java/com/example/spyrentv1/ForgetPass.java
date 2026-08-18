package com.example.spyrentv1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spyrentv1.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ForgetPass extends AppCompatActivity {
    DatabaseHelper myDb;
    private View decorView;
    private TextView Logs;
    private TextInputLayout forgemail,forguname;
    private Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass);

        myDb = new DatabaseHelper(this);
        Logs = (TextView)findViewById(R.id.Logs);
        forguname = (TextInputLayout)findViewById(R.id.forguname);
        forgemail = (TextInputLayout)findViewById(R.id.forgemail);
        submit = (Button)findViewById(R.id.submit);

        Logs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogIn();
            }
        });

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0)
                    decorView.setSystemUiVisibility(hideBar());
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = forguname.getEditText().getText().toString().trim();
                String email = forgemail.getEditText().getText().toString().trim();
                Boolean res = myDb.check( user, email);
                if (res == true) {
                    Toast.makeText(ForgetPass.this,"Redirecting to new password page",Toast.LENGTH_SHORT).show();

                    ArrayList<Integer> data= myDb.sessionid(user);
                    for(int i = 0 ; i < data.size(); i ++) {
                        forg2(data.get(i));
                    }
                }
                else {
                    Toast.makeText(ForgetPass.this,"Wrong Username or Email",Toast.LENGTH_SHORT).show();
                }

                RequestQueue queue = Volley.newRequestQueue(ForgetPass.this);
                String url = "http://spyrents.xyz/res_api/post.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("d")){
                            //Toast.makeText(MainActivity.this,"potaaaaaaaaaaa",Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("data","error server----------->"+error);
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("username",forguname.getEditText().getText().toString());

                        return params;
                    }
                };
                queue.add(stringRequest);







            }

        });










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

    public void LogIn(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void forg2(int username_id){
        Intent intent = new Intent(this, ForgPass2.class);
        intent.putExtra("session_id",username_id);
        startActivity(intent);
    }
}
