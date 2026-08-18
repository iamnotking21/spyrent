package com.example.spyrentv1.ui.gallery;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.spyrentv1.DBManager;
import com.example.spyrentv1.DatabaseHelper;
import com.example.spyrentv1.R;

import com.example.spyrentv1.checkInternetconnections;
import com.google.android.material.textfield.TextInputLayout;

public class GalleryFragment extends Fragment {
    DatabaseHelper myDb;
    DBManager dbManager;
    Dialog mydiag;
    private ListView simpleListviewChild;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        final int id=intent.getIntExtra("session_id",1);

        final String data_pass = myDb.parent_password(id);
        final String data_parent_name = myDb.parent_name(id);

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        final TextInputLayout cname = (TextInputLayout)root.findViewById(R.id.cname);

        Button addchild = (Button) root.findViewById(R.id.addchild);

        final DBManager dbManager = new DBManager(getActivity());
        dbManager.open();




        addchild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String cnamex = cname.getEditText().getText().toString().trim();

                if(!cnamex.isEmpty()){
                    int count_row = myDb.check_if_same_username_child(cnamex);
                    if(count_row>0){
                        checkifInternet(getActivity());
                        Toast.makeText(getActivity(),"Nakuha na ang Username na ito ",Toast.LENGTH_LONG).show();
                    }else{

                        Log.v("data","hindi");
                        if (id>0) {
                            boolean val = myDb.insChild(cnamex, data_pass, 1, id);
                            if(val){
                                checkifInternet(getActivity());
                                Toast.makeText(getActivity(),"Save Successfully",Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getActivity(),"Please Try Again",Toast.LENGTH_LONG).show();
                            }
                        } else {

                            Cursor c = dbManager.fetch_session();
                            String d = c.getString(0);
                            String data_pass = myDb.parent_password(Integer.parseInt(d));
                            boolean val = myDb.insChild(cnamex, data_pass, 1, Integer.parseInt(d));
                            if(val){
                                checkifInternet(getActivity());
                                Toast.makeText(getActivity(),"Save Successfully",Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getActivity(),"Please Try Again",Toast.LENGTH_LONG).show();
                            }

                        }

                    }

                }
                else{
                    Toast.makeText(getActivity(),"Please complete all the fields ",Toast.LENGTH_LONG).show();
                }
            }
        });
        return root;
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
            c.fetchAllChild(context);
            Toast.makeText(context,"meron mobile data",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"wala mobile data",Toast.LENGTH_SHORT).show();
        }
        return connected;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDb = new DatabaseHelper(this.getActivity());
    }


}