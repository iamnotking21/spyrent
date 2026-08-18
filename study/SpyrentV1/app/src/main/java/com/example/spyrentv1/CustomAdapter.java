package com.example.spyrentv1;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.spyrentv1.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CustomAdapter extends BaseAdapter {
    Context context;
    String countryList[];
    String data[];
    LayoutInflater inflater;
    public Switch sw;
    public int uneventstatus=2,eventstatus=1,unstatus=2,status=1,oras,mins,soras,smins,format1,format2,app_id,app_update,nullTimer_update,longidparent,longidchild;
    public String sminutes,sTimeSet,timeSet,minutess,app_name,pack_name;

    public CustomAdapter(Context acontext,ArrayList<String> countryList,ArrayList<String> data,int longidparent,int longidchild){
        this.context = acontext;
        this.longidparent = longidparent;
        this.longidchild = longidchild;
        this.countryList = countryList.toArray(new String[0]);
        this.data = data.toArray(new String[0]);
        inflater = (LayoutInflater.from(acontext));
    }


    @Override
    public int getCount() {
        return countryList.length;
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
    public View getView(int i, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.activity_list,null);

        final DatabaseHelper fetch = new DatabaseHelper(view.getRootView().getContext());

        final ArrayList<String> pangalan_ng_app = fetch.getappname(longidparent,longidchild,status);


        final TextView country = (TextView) view.findViewById(R.id.textview1);
        final TextView datas = (TextView) view.findViewById(R.id.textview2);
        final TextView timerdata = (TextView) view.findViewById(R.id.timertextView3);

        sw = (Switch) view.findViewById(R.id.switch1);

        country.setText(countryList[i]);

        for(int hey = 0; hey < pangalan_ng_app.size(); hey ++){
            if(country.getText().toString().equals(pangalan_ng_app.get(hey))){
                String oras_got = pangalan_ng_app.get(hey);
                ArrayList<String> pasensys_na = fetch.getoras2(longidparent,longidchild,oras_got,status);
                for(int every = 0; every < pasensys_na.size(); every ++){
                    timerdata.setText(pasensys_na.get(every));
                    sw.setChecked(true);
                }
            }
        }

        final ArrayList<String> nullTimerevent = fetch.getappnamenulltimer(longidparent,longidchild,eventstatus);
        for(int pili = 0; pili < nullTimerevent.size(); pili++){
            if(country.getText().toString().equals(nullTimerevent.get(pili))){
                timerdata.setText("Always Blocked until the Accessibility event is open");
                sw.setChecked(true);
            }
        }

        datas.setText(data[i]);

        final View finalView = view;
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if(!country.getText().toString().equals("Google App")){

                        Calendar mcurrentTime = Calendar.getInstance();
                        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                        final int minutes = mcurrentTime.get(Calendar.MINUTE);

                        final TimePickerDialog mTimePicker ;
                        final int finalHour = hour;

                        mTimePicker = new TimePickerDialog(finalView.getRootView().getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                timeSet="";
                                oras = selectedHour;
                                if(oras > 12){
                                    oras-=12;
                                    timeSet = "PM";
                                    format2 =1;
                                }else if(oras == 0){
                                    oras +=12;
                                    timeSet = "AM";
                                    format2 = 0;
                                }else if(oras == 12){
                                    timeSet = "PM";
                                    format2 =1;
                                }else{
                                    timeSet = "AM";
                                    format2 = 0;
                                }
                                minutess = "";
                                mins = selectedMinute;
                                if(mins<10){
                                    minutess = "0"+mins;
                                }else{
                                    minutess = String.valueOf(mins);
                                }
                            }
                        },hour,minutes,false);
                        mTimePicker.setTitle("End");

                        final TimePickerDialog sTimepicker;
                        sTimepicker = new TimePickerDialog(finalView.getRootView().getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int secondHour, int secondMinute) {
                                sTimeSet = "";
                                soras = secondHour;

                                if(soras>12){
                                    soras-=12;
                                    sTimeSet = "PM";
                                    format1 = 1;
                                }else if(soras == 0){
                                    soras +=12;
                                    sTimeSet = "AM";
                                    format1 = 0;
                                }else{
                                    sTimeSet = "AM";
                                    format1 = 0;
                                }

                                sminutes = "";
                                smins = secondMinute;
                                if(smins < 10){
                                    sminutes = "0"+smins;
                                }else{
                                    sminutes = String.valueOf(smins);
                                }
                            }
                        },hour,minutes,false);
                        sTimepicker.setTitle("Start");
                        sTimepicker.show();

                        sTimepicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                if(soras==0){
                                    mTimePicker.cancel();
                                    String appxName = country.getText().toString();
                                    String packxName = datas.getText().toString();
                                    //event status 1 save to database para sa tuwing naka open ang app gagana ang blocking


                                    DatabaseHelper d = new DatabaseHelper(finalView.getRootView().getContext());
                                    long id = d.insertnullTimer(longidparent,longidchild,packxName,appxName,eventstatus);
                                    if(id>=0){

                                        insertNullTimerOnline(finalView.getRootView().getContext(),longidparent,longidchild,packxName,appxName,1);

                                        Toast.makeText(finalView.getRootView().getContext(),"Always Blocked until the Accessibility event is open",Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(finalView.getRootView().getContext(),"Unsuccessfully saved",Toast.LENGTH_SHORT).show();
                                    }



                                }else{
                                    mTimePicker.show();
                                }
                            }
                        });

                        mTimePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                if(oras==0){

                                    mTimePicker.cancel();
                                    //Log.v("data","nito "+country.getText().toString());
                                    String appxName = country.getText().toString();
                                    String packxName = datas.getText().toString();
                                    //event status 1 save to database para sa tuwing naka open ang app gagana ang blocking


                                    DatabaseHelper d = new DatabaseHelper(finalView.getRootView().getContext());
                                    long id = d.insertnullTimer(longidparent,longidchild,packxName,appxName,eventstatus);
                                    if(id>=0){

                                        insertNullTimerOnline(finalView.getRootView().getContext(),longidparent,longidchild,packxName,appxName,1);

                                        Toast.makeText(finalView.getRootView().getContext(),"Always Blocked until the Accessibility event is open",Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(finalView.getRootView().getContext(),"Unsuccessfully saved",Toast.LENGTH_SHORT).show();
                                    }



                                    //gagana ang app hanggat naka open ang event
                                    //value 5
                                }else{

                                    //Log.v("data","time "+oras+mins+format1);
                                    //Log.v("data2","timer "+soras+smins+format2);

                                    //getting and converting the app & package name
                                    app_name = country.getText().toString();
                                    pack_name = datas.getText().toString();

                                    DatabaseHelper d = new DatabaseHelper(finalView.getRootView().getContext());


                                    int data_app = d.getData(longidparent,longidchild,app_name);
                                    //Log.v("data","name "+data_app);
                                    if(data_app<=0){
                                        //Log.v("data","walang laman");

                                        long id = d.insertData1(pack_name,app_name,oras,mins,format2,soras,smins,format1,longidparent,longidchild,status);
                                        if(id>=0){
                                            InsertAppWithTimer(finalView.getRootView().getContext(),longidparent,longidchild,app_name,pack_name,oras,mins,format2,soras,smins,format1);
                                            //Log.v("data","saved");
                                            String data_all = d.getAll(longidparent,longidchild);
                                            //Log.v("data","all "+data_all);
                                        }else{
                                            //Log.v("data","unsaved");
                                        }


                                    }else{
                                        String app_uid = d.getappuid(longidparent,longidchild,app_name);
                                        app_id = Integer.parseInt(app_uid);
                                        // Log.v("data","may laman "+app_uid+" "+app_id);


                                        app_update = d.updateapp_pack(app_id,pack_name,app_name,oras,mins,format2,soras,smins,format1,longidparent,longidchild,status);
                                        if(app_update>=0){
                                            Toast.makeText(finalView.getRootView().getContext(),"Successfully updated ",Toast.LENGTH_SHORT).show();
                                            // Log.v("data","successfully updated");

                                        }else{
                                            Toast.makeText(finalView.getRootView().getContext()," Unsuccessfully updated ",Toast.LENGTH_SHORT).show();
                                            // Log.v("data","unsuccessfully updated");
                                        }



                                    }
                                    soras =0;
                                    oras = 0;

                                }
                            }
                        });

                    }
                }else{
                    //unblock the apps update the fucking database
                    DatabaseHelper d = new DatabaseHelper(finalView.getRootView().getContext());
                    String pota_app = country.getText().toString();
                    //Log.v("data","app "+pota_app);


                    ArrayList<String> apps = d.getappnamenulltimer2(longidparent,longidchild,eventstatus,pota_app);
                    for(int lovesomeone = 0; lovesomeone < apps.size(); lovesomeone++){
                        int idx = Integer.parseInt(apps.get(lovesomeone));

                        updateNullTimer(finalView.getRootView().getContext(),longidparent,longidchild,pota_app);

                        int shingid=d.updatenulltimer(longidparent,longidchild,pota_app,uneventstatus,idx);
                        if(shingid>=0){
                            //Log.v("data","successfully removed");
                        }else{
                            // Log.v("data","unsuccessfully removed");
                        }
                    }



                    //Log.v("naka ","off "+country.getText().toString());
                    String appx_name = country.getText().toString();

                    String app_uid = d.getappuid(longidparent,longidchild,appx_name);
                    if(app_uid.length()!=0){
                        app_id = Integer.parseInt(app_uid);
                        //Log.v("data","may laman "+app_uid+" "+app_id);
                        int appx_update;


                        appx_update = d.updateapp_pack(app_id,pack_name,app_name,oras,mins,format2,soras,smins,format1,longidparent,longidchild,unstatus);

                        updateHaveTimer(finalView.getRootView().getContext(),longidparent,longidchild,appx_name);

                        if(appx_update>=0){
                            Toast.makeText(finalView.getRootView().getContext(),"Successfully updated ",Toast.LENGTH_SHORT).show();
                            //    Log.v("data","successfully removed");

                        }else{
                            Toast.makeText(finalView.getRootView().getContext()," Unsuccessfully updated ",Toast.LENGTH_SHORT).show();
                            //    Log.v("data","unsuccessfully removed");
                        }



                    }

                    //save to database or update database (app table for blocking)
                }
            }
        });

        return view;
    }

    public void InsertAppWithTimer(Context context, int parentidx, int childidx,final String app_namex,final String pack_namex,final int orasx,final int minsx,final int timesetx,final int sorasx,final int sminsx,final int stimesetx){
        DatabaseHelper db = new DatabaseHelper(context);

        final String parent_name = db.parent_name(parentidx);
        final String child_name = db.child_name(childidx);

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/postHaveTimerApp.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response have timer ------------->"+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error response have timer -------->"+error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();

                params.put("parentname",parent_name);
                params.put("childname",child_name);

                params.put("app_name",app_namex);
                params.put("pack_name",pack_namex);

                params.put("oras",String.valueOf(orasx));
                params.put("mins",String.valueOf(minsx));
                params.put("timeset",String.valueOf(timesetx));

                params.put("soras",String.valueOf(sorasx));
                params.put("smins",String.valueOf(sminsx));
                params.put("stimeset",String.valueOf(stimesetx));



                return params;
            }
        };
        queue.add(stringRequest);

    }

    public void updateHaveTimer(Context context, int parentidx, int childidx, final String app_namex){
        DatabaseHelper db = new DatabaseHelper(context);

        final String parentIdxs = db.parent_name(parentidx);
        final String childIdxs = db.child_name(childidx);

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/updateHaveTimer.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response app update have timer-------------->"+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("data","error app update have timer-------------->"+error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();

                params.put("parentname",parentIdxs);
                params.put("childname",childIdxs);
                params.put("appname",app_namex);


                return params;
            }
        };
        queue.add(stringRequest);

    }

    public void updateNullTimer(Context context, int parentidx, int childidx, final String app_namez){
        DatabaseHelper db = new DatabaseHelper(context);

        final String parentIdxs = db.parent_name(parentidx);
        final String childIdxs = db.child_name(childidx);

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/updateNulltimer.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response nulltimer---------->"+response);
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

                params.put("parentname",parentIdxs);
                params.put("childname",childIdxs);
                params.put("appname",app_namez);

                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void insertNullTimerOnline(Context context, int parentidx, int childidx, final String packnamez, final String app_namez, final int eventstatusz){
        DatabaseHelper db = new DatabaseHelper(context);

        final String parentIdxs = db.parent_name(parentidx);
        final String childIdxs = db.child_name(childidx);

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://spyrents.xyz/res_api/postnullTimer.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("data","response nulltimer---------->"+response);
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

                params.put("parentname",parentIdxs);
                params.put("childname",childIdxs);
                params.put("packname",packnamez);
                params.put("appname",app_namez);
                params.put("eventstatus",String.valueOf(eventstatusz));


                return params;
            }
        };
        queue.add(stringRequest);
    }
}
