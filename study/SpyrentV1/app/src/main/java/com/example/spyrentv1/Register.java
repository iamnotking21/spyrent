package com.example.spyrentv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spyrentv1.R;
import com.google.android.material.textfield.TextInputLayout;

public class Register extends AppCompatActivity {
    private View decorView;
    DatabaseHelper myDb;
    private TextView Logs;
    private TextInputLayout fname,lname,email,uname,pword;
    private EditText pos;
    private Button sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        myDb = new DatabaseHelper(this);

        fname=(TextInputLayout)findViewById(R.id.fname);
        lname=(TextInputLayout)findViewById(R.id.lname);
        email=(TextInputLayout)findViewById(R.id.email);
        uname=(TextInputLayout)findViewById(R.id.uname);
        pword=(TextInputLayout)findViewById(R.id.pword);

        pos=(EditText) findViewById(R.id.pos);
        sub=(Button)findViewById(R.id.sub);
        Logs = (TextView)findViewById(R.id.Logs);
        pos.setText("parent");
        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0)
                    decorView.setSystemUiVisibility(hideBar());
            }
        });


        Logs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogIn();
            }
        });
    AddData();

    }

    public void AddData(){
        sub.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String num = email.getEditText().getText().toString().trim();
                        String fn = fname.getEditText().getText().toString().trim();
                        String ln = lname.getEditText().getText().toString().trim();
                        String un = uname.getEditText().getText().toString().trim();
                        String pw = pword.getEditText().getText().toString().trim();
                        if (fn.length() < 4) {
                            Toast.makeText(Register.this, "Your Firstname is Invalid", Toast.LENGTH_LONG).show();
                        }
                        if (ln.length() < 4) {
                            Toast.makeText(Register.this, "Your Lastname is invalid", Toast.LENGTH_LONG).show();
                        }
                        if (num.length() < 11) {
                            Toast.makeText(Register.this, "Your number is invalid", Toast.LENGTH_LONG).show();
                        }
                        if (un.length() < 8) {
                            Toast.makeText(Register.this, "Your Username is invalid", Toast.LENGTH_LONG).show();
                        }
                        if (pw.length() < 8) {
                            Toast.makeText(Register.this, "Your Password is invalid", Toast.LENGTH_LONG).show();
                        }
                        else {
                            int count_row = myDb.checkifsameUsernameandPassword(uname.getEditText().getText().toString(),pword.getEditText().getText().toString());
                            if (count_row != 0) {
                                Toast.makeText(Register.this, "Information is existed.Please choose Another", Toast.LENGTH_LONG).show();
                            } else {
                                Log.v("data", "wala");
                                boolean isInserted = myDb.insertD(fn, ln, num, un, pw, pos.getText().toString());
                                if (isInserted = true) {

                                    checkifInternet(Register.this);

                                    Toast.makeText(Register.this, "Registered Successfully!", Toast.LENGTH_LONG).show();
                                    LogIn();
                                } else
                                    checkifInternet(Register.this);
                                Toast.makeText(Register.this, "Registered Not Successfully!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
        );
    }

    public boolean checkifInternet(Context context){
        checkInternetconnections c = new checkInternetconnections();
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
            c.fetchAllUsernameParent(context);
            Toast.makeText(context,"meron mobile data",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"wala mobile data",Toast.LENGTH_SHORT).show();
        }
        return connected;
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
}
