package com.example.spyrentv1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spyrentv1.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class webCustomAdapter extends BaseAdapter {

    Context context;
    String domain[],domainid[];
    int parentid,childid;
    LayoutInflater inflater;

    public webCustomAdapter(Context acontext, ArrayList<String> domain, ArrayList<String> domainid,int parentid,int childid){
        this.context = acontext;
        this.parentid = parentid;
        this.childid = childid;
        this.domainid = domainid.toArray(new String[0]);
        this.domain = domain.toArray(new String[0]);
        inflater = (LayoutInflater.from(acontext));
    }

    @Override
    public int getCount() {
        return domain.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.block_website_activity_site,null);
        AssetManager am = context.getApplicationContext().getAssets();
        Typeface custom_font = Typeface.createFromAsset(am,  "fonts/kindi.otf");
        final TextView domain_namex = (TextView) view.findViewById(R.id.webtextview1);
        final Button btndel = (Button) view.findViewById(R.id.delbutton10);
        final Button btnedit = (Button) view.findViewById(R.id.editbutton8);

        final DatabaseHelper wx = new DatabaseHelper(view.getRootView().getContext());

        final String parentIdx = wx.parent_name(parentid);
        final String childIdx = wx.child_name(childid);
        domain_namex.setTypeface(custom_font);
        btndel.setTypeface(custom_font);
        btnedit.setTypeface(custom_font);
        domain_namex.setText(domainid[i]+". "+domain[i]);

        btndel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                builder.setTitle("Are you sure? Do you want to remove this ? "+domain[i]);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int iz) {
                        Log.v("data","id: "+domainid[i]);
                        int uid = Integer.parseInt(domainid[i]);
                        int val_id = wx.update_web_unstatus(uid,parentid,childid,2);
                        if(val_id>=0){
                            update_unstatus_web(view.getRootView().getContext(),domain[i],parentIdx,childIdx);
                            Toast.makeText(view.getRootView().getContext(),"Removed Successfully",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(view.getRootView().getContext(),"Please Try Again ",Toast.LENGTH_SHORT).show();
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


        btnedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                builder.setTitle(" Edit Domain");

                final EditText input = new EditText(view.getRootView().getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(domain[i]);

                builder.setView(input);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int iz) {
                        if(!input.getText().toString().isEmpty()){
                            int idx = Integer.parseInt(domainid[i]);
                            Log.v("data","id "+idx);
                            int id_update_web = wx.update_web(idx,parentid,childid,1,input.getText().toString());
                            if(id_update_web>=0){
                                Update_website(view.getRootView().getContext(),domain[i],input.getText().toString(),parentIdx,childIdx);

                                Toast.makeText(view.getRootView().getContext(),"Update Successfully",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(view.getRootView().getContext(),"Unsuccessfully",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(view.getRootView().getContext(),"Please Complete the field",Toast.LENGTH_SHORT).show();
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

        return view;
    }

    public void Update_website(Context context, final String oldname, final String new_name, final String parentname, final String childname){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/update_website.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response web update "+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error "+error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("oldname",oldname);
                params.put("new_name",new_name);
                params.put("parentname",parentname);
                params.put("childname",childname);
                params.put("unstatus","status");

                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void update_unstatus_web(Context context, final String oldname,final String parentname,final String childname){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/update_website.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response web update "+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error "+error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("oldname",oldname);
                params.put("parentname",parentname);
                params.put("childname",childname);
                params.put("unstatus","unstatus");

                return params;
            }
        };
        queue.add(stringRequest);
    }

}
