package com.example.spyrentv1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    DatabaseHelper myDb;
    private View decorView;
    private ImageView forg;
    private TextView forgs,pos,sess_id,register;
    private TextInputLayout uname,pword;
    private Button log;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDb = new DatabaseHelper(this);

        final DBManager dbManager = new DBManager(this);
        dbManager.open();

        uname = (TextInputLayout)findViewById(R.id.uname);
        pword = (TextInputLayout)findViewById(R.id.pword);

        log = (Button)findViewById(R.id.log);
        forgs = (TextView)findViewById(R.id.forgs);
        forg = (ImageView) findViewById(R.id.forg);
        register = (TextView) findViewById(R.id.register);
        pos = (TextView)findViewById(R.id.pos);
        sess_id = (TextView)findViewById(R.id.sess_id);

        session = new Session(this);
        pos.setText("parent");

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSyste/**/mUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0)
                    decorView.setSystemUiVisibility(hideBar());
            }
        });

        if (session.loggedin()){
            Intent intent = new Intent(this, Parent_panel.class);
            int username_id = 0;
            intent.putExtra("session_id",username_id);
            startActivity(intent);
            finish();
        }

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = uname.getEditText().getText().toString().trim();
                String pass = pword.getEditText().getText().toString().trim();
                String post = pos.getText().toString().trim();
                Boolean res = myDb.checker( user, pass, post);
                if (res == true) {
                    session.setLoggedin(true);
                    Toast.makeText(MainActivity.this,"Successfully Logged In . . .",Toast.LENGTH_SHORT).show();
                    ArrayList<Integer> data= myDb.sessionid(user);
                    for(int i = 0 ; i < data.size(); i ++){
                        panel(data.get(i));

                    }
                }
                else {
                    Toast.makeText(MainActivity.this,"Wrong Username or Password",Toast.LENGTH_SHORT).show();
                }
            }

        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegsView();
            }
        });
        forgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forget();
            }
        });
        forg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forget();
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
    public void RegsView(){
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }
    public void panel(int username_id){
        Intent intent = new Intent(this, Parent_panel.class);
        intent.putExtra("session_id",username_id);
        startActivity(intent);
    }
    public void forget(){
        Intent intent = new Intent(this, ForgetPass.class);
        startActivity(intent);
    }
}
