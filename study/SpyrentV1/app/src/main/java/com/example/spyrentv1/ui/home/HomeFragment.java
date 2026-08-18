package com.example.spyrentv1.ui.home;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.spyrentv1.DBManager;
import com.example.spyrentv1.DatabaseHelper;
import com.example.spyrentv1.R;
import com.example.spyrentv1.batalist;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private ListView simpleListView;
    private HomeViewModel homeViewModel;
    DatabaseHelper myDb;
    ArrayAdapter arrayAdapter;
    DBManager dbManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        int id = intent.getIntExtra("session_idx",0);
        int childid = intent.getIntExtra("child_id",0);



        DBManager dbManager = new DBManager(getActivity());
        dbManager.open();



        View root = inflater.inflate(R.layout.fragment_home, container, false);

        if (id>0) {

            ArrayList<String> child_name = myDb.childs_name(id,1);
            ArrayList<String> datachildid = myDb.childs_id(id,1);
            simpleListView = (ListView) root.findViewById(R.id.simplechilddata);
            final batalist wx = new batalist(getActivity(),child_name,datachildid,id);
            simpleListView.setAdapter(wx);
            Log.v("data","child id "+childid);

        }
        else {
            Cursor c = dbManager.fetch_session();
            String d = c.getString(0);
            Log.v("data", "wakawaka------------->" + d);
            Cursor cursor = dbManager.fetch(Integer.parseInt(d));
            cursor.moveToFirst();
            ArrayList<String> child_name = myDb.childs_name(Integer.parseInt(d),1);
            ArrayList<String> datachildid = myDb.childs_id(Integer.parseInt(d),1);
            simpleListView = (ListView) root.findViewById(R.id.simplechilddata);
            final batalist wx = new batalist(getActivity(),child_name,datachildid,Integer.parseInt(d));
            simpleListView.setAdapter(wx);

        }

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDb = new DatabaseHelper(getActivity());
    }
}