package com.example.spyrentv1.ui.slideshow;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.spyrentv1.DBManager;
import com.example.spyrentv1.DatabaseHelper;
import com.example.spyrentv1.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class SlideshowFragment extends Fragment {
    DBManager dbManager;
    DatabaseHelper myDb;
    private SlideshowViewModel slideshowViewModel;
    private TextInputLayout firstname,lastname,email,cpword;
    private TextView username;
    private Button save;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        final int id=intent.getIntExtra("session_id",1);
        final DatabaseHelper myDb = new DatabaseHelper(getActivity());
        final DBManager dbManager = new DBManager(getActivity());
        dbManager.open();

        Cursor c = dbManager.fetch_session();
        String d = c.getString(0);
        final String data_pass = myDb.parent_username(Integer.parseInt(d));


        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        firstname =(TextInputLayout)root.findViewById(R.id.firstname);
        username =(TextView)root.findViewById(R.id.username);
        lastname =(TextInputLayout)root.findViewById(R.id.lastname);
        email =(TextInputLayout)root.findViewById(R.id.email);
        cpword =(TextInputLayout)root.findViewById(R.id.cpword);

        username.setText(data_pass);
        save =(Button) root.findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = data_pass;
                String fname = firstname.getEditText().getText().toString().trim();
                String lname = lastname.getEditText().getText().toString().trim();
                String number = email.getEditText().getText().toString().trim();
                String password = cpword.getEditText().getText().toString().trim();

                String cpword1 = cpword.getEditText().getText().toString().trim();
                if (id>0) {

                    dbManager.update2(id, user, fname,lname,number,password);
                    dbManager.update_child_account(id,cpword1);

                    updateAccount(getActivity(),id, user, fname,lname,number,password);
                    Toast.makeText(getActivity(), "Password Successfully Change!", Toast.LENGTH_SHORT).show();

                }
                else{
                    Cursor c = dbManager.fetch_session();
                    String d = c.getString(0);
                    dbManager.update2(Integer.parseInt(d), user, fname,lname,number,password);
                    dbManager.update_child_account(Integer.parseInt(d),cpword1);
                    updateAccount(getActivity(),Integer.parseInt(d), user, fname,lname,number,password);
                    Toast.makeText(getActivity(), "Password Successfully Change!", Toast.LENGTH_SHORT).show();

                    Toast.makeText(getActivity(), "1st and 2nd password not same!", Toast.LENGTH_SHORT).show();

                }
            }
        });

        return root;
    }

    public void updateAccount(Context context,final int id,final String user,final String fname,final String lname, final String number, final String password){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/updateUserChild.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response update ok --------------------------> "+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error update ------------------------------>"+error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("usern",user);
                params.put("em",number);
                params.put("firstname",fname);
                params.put("lastname",lname);
                params.put("cpword1",password);

                return params;
            }
        };
        queue.add(stringRequest);

    }

}