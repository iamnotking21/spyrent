package com.example.spyrentv1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spyrentv1.R;

public class ForgPass2 extends AppCompatActivity {
    DatabaseHelper myDb;
    private View decorView;
    private TextView passwordssss;
    private Button submit2;
    DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forg_pass2);
        myDb = new DatabaseHelper(this);

        final DBManager dbManager = new DBManager(this);
        dbManager.open();


        Intent intent = this.getIntent();
        final int id=intent.getIntExtra("session_id",1);
        final String data_pass = myDb.parent_password(id);
        Log.v("data","session_id------------->"+id );
        passwordssss = (TextView)findViewById(R.id.passwordssss);
        passwordssss.setText(data_pass);


        submit2 = (Button)findViewById(R.id.submit2);


        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0)
                    decorView.setSystemUiVisibility(hideBar());
            }
        });

        submit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    LogIn();

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
}
