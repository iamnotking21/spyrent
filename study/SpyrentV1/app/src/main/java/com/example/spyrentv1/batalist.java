package com.example.spyrentv1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.spyrentv1.R;

import java.util.ArrayList;

public class batalist extends BaseAdapter {
    DBManager dbManager;
    Context context;
    String child_name[],childid[];
    int parentid;

    LayoutInflater inflater;

    public batalist(Context acontext, ArrayList<String> child_name, ArrayList<String> childid, int parentid){
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


        view = inflater.inflate(R.layout.activity_batalist,null);
        final TextView child_namesx = (TextView) view.findViewById(R.id.webtextview1);
        final TextView set_Action = (Button) view.findViewById(R.id.set_Action);
        final DatabaseHelper db = new DatabaseHelper(view.getRootView().getContext());

        child_namesx.setText(child_name[i]);

        final DBManager dbManager = new DBManager(view.getRootView().getContext());
        dbManager.open();




        final View finalView = view;

        set_Action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v("data","apps "+childid[i]+" parentid "+parentid);
                //Log.v("data","websites "+childid[i]+" parentid "+parentid);

                /*Cursor cursor = dbManager.fetch_child(parentid);
                if(cursor.moveToFirst()){
                    do {
                        String name = cursor.getString(cursor.getColumnIndex("CNAME"));
                        Log.v("data","name ng bata  ------->"+name);
                    }while (cursor.moveToNext());
                }cursor.close();*/
                Log.v("data","id ng bata ----------->"+child_name[i]);


                Intent intent = new Intent(finalView.getRootView().getContext(), ChildsList.class);
                if(parentid>0) {
                    ArrayList<String> data_child = dbManager.fetch_child_arrayList(child_name[i]);
                    Log.v("data","childs name "+data_child);
                    String data_id = String.valueOf(data_child);

                    Log.v("data","iddddddddddddddd"+data_id);
                    intent.putExtra("session_idx", parentid);
                    intent.putExtra("session_id_child", data_child);
                    intent.putExtra("child_name", child_name[i]);
                    intent.putExtra("child_id",data_id);
                    Log.v("data","child id "+data_child);
                    Log.v("data","laman---------->"+data_child);
                    finalView.getRootView().getContext().startActivity(intent);

                } else {

                    ArrayList<String> data_child = dbManager.fetch_child_arrayList(child_name[i]);
                    Log.v("data","childs name "+data_child);
                    String data_id = String.valueOf(data_child);
                    Log.v("data","iddddddddddddddd"+data_id);

                    Cursor c = dbManager.fetch_session();
                    String d = c.getString(0);
                    Log.v("data", "wakawaka------------->" + d);
                    Cursor cursor = dbManager.fetch(Integer.parseInt(d));
                    Log.v("data","child id "+data_child);
                    intent.putExtra("session_idx", parentid);
                    intent.putExtra("session_id_child", data_child);
                    intent.putExtra("child_name", child_name[i]);
                    intent.putExtra("child_id",data_id);
                    finalView.getRootView().getContext().startActivity(intent);

                    //Log.v("data","laman---------->"+data_child);

                }
            }
        });
        //edi

        return view;
    }


}
