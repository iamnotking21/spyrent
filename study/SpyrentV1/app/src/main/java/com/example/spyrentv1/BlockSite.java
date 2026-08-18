package com.example.spyrentv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class BlockSite extends AppCompatActivity {
    private Button btn1;
    private TextInputLayout ed1;
    private ImageView back;
    private ListView simple_website;
    private ListView simplelistweb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_site);
        TextView childNames = (TextView) findViewById(R.id.nameofchild);
        back = (ImageView)findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balik();
            }
        });
        final DatabaseHelper w = new DatabaseHelper(BlockSite.this);

        Intent i = getIntent();
        final int parentid = i.getIntExtra("session_idx",0);
        final int child = i.getIntExtra("session_id_child",0);

        final RequestQueue queue = Volley.newRequestQueue(BlockSite.this);
        final String url = "http://spyrents.xyz/res_api/postWebsite.php";

        ArrayList<String> dataChildName = w.data_name(parentid,child,1);
        for(int ix = 0; ix < dataChildName.size(); ix++){
            childNames.setText(dataChildName.get(ix));
        }

        Log.v("data","val parentid id: "+parentid+" child id : "+child);

        ed1 = (TextInputLayout) findViewById(R.id.editText);

        btn1 = (Button) findViewById(R.id.button9);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ed1.getEditText().getText().toString().isEmpty()){
                    String domain = ed1.getEditText().getText().toString();
                    int count_row_web = w.getCountweb(domain,parentid,child,1);
                    if(count_row_web!=0){
                        Log.v("data","meron ");
                    }else{

                        long id = w.insertData(domain,1,parentid,child);
                        if(id>=0){

                            getUnameParent(parentid,child,w,url,queue,domain,1);

                            Toast.makeText(BlockSite.this,"saved successfully",Toast.LENGTH_SHORT).show();
                            Log.v("data","saved successfully");
                            ed1.getEditText().setText("");
                        }else{
                            Toast.makeText(BlockSite.this,"unsuccessfully saved",Toast.LENGTH_SHORT).show();
                            Log.v("data","unsuccessfully saved");
                        }

                    }
                }else{
                    Toast.makeText(BlockSite.this,"Please Complete the Field",Toast.LENGTH_SHORT).show();
                }
            }
        });


        ArrayList<String> domainx = w.getAll(parentid,child,1);
        ArrayList<String> domainid = w.getAllID(parentid,child,1);

        simplelistweb = (ListView) findViewById(R.id.simpleblockwebsite);
        final webCustomAdapter webx = new webCustomAdapter(getApplicationContext(),domainx,domainid,parentid,child);
        simplelistweb.setAdapter(webx);

    }

    public void getUnameParent(int id, int chillidx, DatabaseHelper w, String url, RequestQueue queue, final String domain, final int status_child){
        final String parentId = w.parent_name(id);
        final String childId = w.child_name(chillidx);
        Log.v("data","parent uname "+parentId+" child name "+childId);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response----------------->"+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error response "+error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("parent_name",parentId);
                params.put("child_name",childId);
                params.put("domain",domain);
                params.put("status_child",String.valueOf(status_child));
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void balik(){
        Intent intent = new Intent(this, Parent_panel.class);
        startActivity(intent);
    }
}
