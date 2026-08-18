package com.example.spyrentv1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.spyrentv1.R;
import com.google.android.material.textfield.TextInputLayout;

public class Creg extends AppCompatActivity {
    DatabaseHelper myDb;
    private Button addchild;
    private TextInputLayout cname,chpass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_gallery);

        myDb = new DatabaseHelper(this);

        cname = (TextInputLayout)findViewById(R.id.cname);
        chpass = (TextInputLayout)findViewById(R.id.chpass);
        addchild = (Button) findViewById(R.id.addchild);

        //AddCData();
    }


    public void AddCData(){
        addchild.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /*boolean isInserted = myDb.insChild(cname.getEditText().getText().toString(),
                                chpass.getEditText().getText().toString());
                        if(isInserted){
                            Toast.makeText(Creg.this,"Registered Successfully!",Toast.LENGTH_LONG).show();
                            Log.v("data","inserted");
                        }
                        else{
                            Log.v("data","not inserted");
                            Toast.makeText(Creg.this,"Registered Not Successfully!",Toast.LENGTH_LONG).show();
                        }*/

                    }
                }
        );
    }
}
