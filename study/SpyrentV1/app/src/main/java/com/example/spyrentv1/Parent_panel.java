package com.example.spyrentv1;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.spyrentv1.R;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

public class Parent_panel extends AppCompatActivity {
    Dialog mydiag;
    DatabaseHelper myDb;
    private Button addchild;
    private TextInputLayout cname,chpass;
    private Session session;
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_panel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        int id = i.getIntExtra("session_id",1);

        Log.v("data","session_id------------->"+id );

        DBManager dbManager = new DBManager(this);
        dbManager.open();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView name = (TextView) headerView.findViewById(R.id.name);
        TextView email = (TextView) headerView.findViewById(R.id.email);
        if (id>0) {
            Cursor cursor = dbManager.fetch(id);
            dbManager.insert(id);
            cursor.moveToFirst();
            name.setText(cursor.getString(0)+" "+cursor.getString(1));
            email.setText(cursor.getString(2));
        }
        else {
            Cursor c = dbManager.fetch_session();
            String d = c.getString(0);
            Log.v("data", "wakawaka------------->" + d);
            Cursor cursor = dbManager.fetch(Integer.parseInt(d));
            cursor.moveToFirst();
            name.setText(cursor.getString(0)+" "+cursor.getString(1));
            email.setText(cursor.getString(2));
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mydiag = new Dialog(this);
        myDb = new DatabaseHelper(this);

            cname = (TextInputLayout)findViewById(R.id.cname);
            chpass = (TextInputLayout)findViewById(R.id.chpass);
            addchild = (Button) findViewById(R.id.addchild);


        session = new Session(this);
        if (!session.loggedin()){
            logout_s();
        }
    }



    public void AddCData(){
        addchild.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*
                        boolean isInserted = myDb.insChild(cname.getEditText().getText().toString(),
                                chpass.getEditText().getText().toString());
                        if(isInserted)
                            Toast.makeText(Parent_panel.this,"Registered Successfully!",Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(Parent_panel.this,"Registered Not Successfully!",Toast.LENGTH_LONG).show();
                        */
                    }
                }
        );
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.action_settings){
            DBManager dbManager = new DBManager(this);
            dbManager.open();
                dbManager.delete();

            logout_s();
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }




    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void childreg(){
        Intent intent = new Intent(this, Creg.class);
        startActivity(intent);
    }
    private void logout_s(){
        session.setLoggedin(false);
        finish();
        startActivity(new Intent(this,MainActivity.class));
    }
    private void home(){
        startActivity(new Intent(this,MainActivity.class));
    }

}
