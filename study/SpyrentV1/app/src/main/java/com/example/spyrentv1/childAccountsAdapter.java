package com.example.spyrentv1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class childAccountsAdapter extends BaseAdapter {
    Context context;
    String child_name[],childid[];
    int parentid;
    LayoutInflater inflater;

    public childAccountsAdapter(Context acontext, ArrayList<String> child_name, ArrayList<String> childid, int parentid){
        this.context = acontext;
        this.child_name = child_name.toArray(new String[0]);
        this.childid = childid.toArray(new String[0]);
        this.parentid = parentid;
        inflater = (LayoutInflater.from(acontext));
    }

    @Override
    public int getCount() {
        return child_name.length;
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
    public View getView(final int i, View view, final ViewGroup parent) {
        view = inflater.inflate(R.layout.childs_accounts_anak_nanay_mo,null);

        AssetManager am = context.getApplicationContext().getAssets();
        Typeface custom_font = Typeface.createFromAsset(am,  "fonts/kindi.otf");

        final TextView child_namesx = (TextView) view.findViewById(R.id.webtextview1);
        final TextView child_names = (TextView) view.findViewById(R.id.webtextview2);

        final Button btndel = (Button) view.findViewById(R.id.delbutton10);
        final Button btnedit = (Button) view.findViewById(R.id.editbutton8);
        final Button btnapps = (Button) view.findViewById(R.id.apps);
        final Button btnsites = (Button) view.findViewById(R.id.websites);

        final ImageView image1 = (ImageView) view.findViewById(R.id.image1);
        image1.setImageResource(R.drawable.blockingandtimelabel1);

        final Button btnhistory_app = (Button) view.findViewById(R.id.history_app);
        final Button btnhistory_sites = (Button) view.findViewById(R.id.history_sites);
        final ImageView image2 = (ImageView) view.findViewById(R.id.image2);

        image2.setImageResource(R.drawable.viewhistory1);

        final DatabaseHelper db = new DatabaseHelper(view.getRootView().getContext());
        child_namesx.setTypeface(custom_font);
        child_names.setTypeface(custom_font);
        btndel.setTypeface(custom_font);
        btnapps.setTypeface(custom_font);
        btnedit.setTypeface(custom_font);
        btnsites.setTypeface(custom_font);
        btnhistory_app.setTypeface(custom_font);
        btnhistory_sites.setTypeface(custom_font);
        child_namesx.setText(child_name[i]);

        final View finalView = view;
        btnapps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("data","apps "+childid[i]+" parentid "+parentid);
                Log.v("data","websites "+childid[i]+" parentid "+parentid);

                Intent intent = new Intent(finalView.getRootView().getContext(), content.class);
                intent.putExtra("session_idx",parentid);
                intent.putExtra("session_id_child",Integer.parseInt(childid[i]));
                intent.putExtra("child_name",child_name[i]);
                finalView.getRootView().getContext().startActivity(intent);
            }
        });

        //app
        btnhistory_sites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(finalView.getRootView().getContext(), history_sites.class);
                intent.putExtra("session_idx",parentid);
                intent.putExtra("session_id_child",Integer.parseInt(childid[i]));
                intent.putExtra("child_name",child_name[i]);
                finalView.getRootView().getContext().startActivity(intent);
            }
        });

        //sites
        btnhistory_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(finalView.getRootView().getContext(), history_app.class);
                intent.putExtra("session_idx",parentid);
                intent.putExtra("session_id_child",Integer.parseInt(childid[i]));
                intent.putExtra("child_name",child_name[i]);
                finalView.getRootView().getContext().startActivity(intent);
            }
        });


        btnsites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("data","websites "+childid[i]+" parentid "+parentid);
                Intent intent = new Intent(finalView.getRootView().getContext(), BlockSite.class);
                intent.putExtra("session_idx",parentid);
                intent.putExtra("session_id_child",Integer.parseInt(childid[i]));
                finalView.getRootView().getContext().startActivity(intent);
            }
        });


        btndel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                builder.setTitle("Are you sure? Do you want to remove this ? "+child_name[i]);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int iz) {
                        Log.v("data","id: "+childid[i]);
                        int uid = Integer.parseInt(childid[i]);

                        update_status(view.getRootView().getContext(),child_name[i]);

                        int count_del_row = db.update_child_unactive(2,parentid,uid);
                        if(count_del_row>0){
                            Toast.makeText(view.getRootView().getContext(),"Successfully Removed",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(view.getRootView().getContext(),"Please Try Again Later",Toast.LENGTH_LONG).show();
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


        //edit
        btnedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                builder.setTitle(" Do you want to Change this username ?");

                final EditText input = new EditText(view.getRootView().getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(child_name[i]);

                builder.setView(input);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int iz) {
                        if(!input.getText().toString().isEmpty()){
                            String new_user = input.getText().toString();
                            int idx = Integer.parseInt(childid[i]);

                            getsqliteid_primaryKeyonline(view.getRootView().getContext(),new_user,child_name[i],idx);

                            //Log.v("data","id "+idx+"     "+new_user);
                            int count_same = db.check_if_same_username_child(new_user);
                            if(count_same>0){
                                Toast.makeText(view.getRootView().getContext(),"Ang username na ito ay nakuha na",Toast.LENGTH_LONG).show();
                            }else{
                                //Log.v("data","not same username");
                                int count = db.update_username_child(new_user,parentid,idx);
                                if(count>=0){

                                    Toast.makeText(view.getRootView().getContext(),"Update Successfully",Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(view.getRootView().getContext(),"Please Try Again Later",Toast.LENGTH_LONG).show();
                                }
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
    public void getsqliteid_primaryKeyonline(Context context, final String uname_child,final String oldname,final int sqliteid){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/getsqliteid_primaryKeyonline.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response "+response);
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
                params.put("uname",uname_child);
                params.put("sqliteid",String.valueOf(sqliteid));
                params.put("oldname",oldname);

                return params;
            }
        };
        queue.add(stringRequest);

    }

    //update unstatus
    public void update_status(Context context,final String oldname){
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/update_child_unstatus.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response---------------- del remove child names "+response);
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

                return params;
            }
        };
        queue.add(stringRequest);

    }


}
