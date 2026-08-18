package com.ax.childapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyService extends AccessibilityService {
    private PackageManager packageManager;
    private List<ApplicationInfo> applist,xapp ;
    public static final int parentid=12,childid=1;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;

    public static final String data = "start timer";
    private int parentid_a=12,childid_a=1,status=1,am_pm_c,soras,smins,sformat,p_uid;
    public Calendar calendar_a;

    public static final String data_e = "End timer";
    private int parentid_e=12,childid_e=1,status_e=1,am_pm_c_e,oras_e,mins_e,format_e,p_uid_e,timestatus_e,c_uid_e;
    private String app_name;
    public Calendar calendar_e;

    block_list b = new block_list();
    ArrayList<String> websites = b.block_website_list();

    adult_app_content a = new adult_app_content();
    ArrayList<String> app_adult = a.adult_games_app();

    block_list_part2_web bprt2 = new block_list_part2_web();
    ArrayList<String> b_block = bprt2.block_list_part2();




    public MyService() {
    }



    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();


        websiteAdapter db = new websiteAdapter(MyService.this);

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = null;

        setServiceInfo(info);


        packageManager = getPackageManager();
        ArrayList<String> app_name_save_phone = xinstalledapps();


        Log.v("connected","event--------->"+app_name_save_phone.size());

        int count_row = db.getcountrow();

        int size = getarrayChanged();

        for(int x = 0; x < app_name_save_phone.size(); x++){
            //Log.v("data","app Name "+app_name_save_phone.get(x));
            //Log.v("size","app "+size);
            if(count_row==0){
                //Toast.makeText(MyService.this,"Please wait scanning.........",Toast.LENGTH_SHORT).show();
                boolean val = checkSameNameApp(parentid,childid,app_name_save_phone.get(x));
                if(val){
                    //Log.v("data","may laman----------->");
                }else{
                    //Log.v("data","walang laman--------------->");

                    saveInstalled(parentid,childid,app_name_save_phone.get(x));
                   // Toast.makeText(MyService.this,"Scanning complete",Toast.LENGTH_SHORT).show();
                }

            }else if(size==app_name_save_phone.size()){

                //Log.v("data","la lang------------22");

            }else if(size<app_name_save_phone.size()){

                boolean val = checkSameNameApp(parentid,childid,app_name_save_phone.get(x));
                if(val){
                    //Log.v("data","may laman----------->");
                }else{
                    //Log.v("data","walang laman--------------->");
                    saveInstalled(parentid,childid,app_name_save_phone.get(x));
                }

            }else if(size>app_name_save_phone.size()){

                //Log.v("data","delete----------------------55");
                if(db.delete_all_apps_installed()){
                    //Log.v("successfully ","delete--------------------------------->66");

                }
            }

        }


        Log.v("size"," website "+websites.size());
        Log.v("size"," app "+app_adult.size());

    }

    @SuppressLint("WrongConstant")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final ArrayList<String> block_list_web_parent = new ArrayList<>();
        ArrayList<String> block_list_app_child = new ArrayList<>();
        ArrayList<String> pack_name_have_timer = new ArrayList<>();
        final websiteAdapter wx = new websiteAdapter(MyService.this);


        //current date

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss aa");
        date = dateFormat.format(calendar.getTime());






        //array list for browser app blocking
        ArrayList<String> browser_app_block = new ArrayList<>();

        browser_app_block.add("com.UCMobile.intl");
        browser_app_block.add("com.brave.browser");
        browser_app_block.add("org.mozilla.firefox");
        browser_app_block.add("com.opera.browser");
        browser_app_block.add("com.dv.adm");
        browser_app_block.add("com.duckduckgo.mobile.android");

        if(event.getEventTime()!=0){

            //Log.v("data","event time ---------------------------------->"+event.getEventTime());



            //null timer app
            Cursor child_null_timer_app = wx.fetchnulltimer();
            if(child_null_timer_app.moveToFirst()){
                do{
                    String pack_name = child_null_timer_app.getString(child_null_timer_app.getColumnIndex("apps_pack"));
                    block_list_app_child.add(pack_name);
                    //Log.v("data","block list pack_name "+pack_name);
                }while(child_null_timer_app.moveToNext());
            }child_null_timer_app.close();



            Cursor cursor_web = wx.fetchdomain();

            if(cursor_web.moveToFirst()){
                do {
                    String domain_name = cursor_web.getString(cursor_web.getColumnIndex("domain"));
                    Log.v("data","domain name ----------------------------------->"+domain_name);
                    block_list_web_parent.add(domain_name);
                }while (cursor_web.moveToNext());
            }cursor_web.close();


            //block apps with timer


            //pack_name_have_timer = wx.pack_name_have_timer(3);
            /*
            for(int n = 0 ; n < pack_name_have_timer.size(); n++){

                Log.v("data","timer status "+pack_name_have_timer.get(n));

            }
            */


        }

        AccessibilityNodeInfo currentNode = getRootInActiveWindow();
        AccessibilityNodeInfo source = event.getSource();

        if(source == null){
            return;
        }

        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            for(int bro = 0; bro < browser_app_block.size(); bro ++){

                if(source.getPackageName()!=null){
                    if (source.getPackageName().equals(browser_app_block.get(bro))){

                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(startMain);

                        startService(new Intent(this, alert.class));

                    }else{

                    }
                }

            }
            //history app



            if(source.getPackageName()!=null){
                ArrayList<String> datad = checkifnakainstalled(source.getPackageName().toString());
                for(int v = 0; v < datad.size();  v++){
                    Log.v("data","---------------------->"+datad.get(v));

                    /*
                    int count_histtory_app = wx.checkIfsameHistoryApp(datad.get(v),date);
                    if(count_histtory_app != 0){
                        Log.v("data","meron");
                    }else{
                        Log.v("data","wala");

                        long id = wx.insertHistory_app(datad.get(v),date,parentid,childid);
                        if(id>=0){
                            Log.v("data","successfully save app history---------------------->"+datad.get(v)+" ---->"+date);
                        }else{
                            Log.v("data","not successsuflyl save");
                        }

                    }
                    */
                }
            }



            //app have timer fucking bullshit
            Cursor cursor_h = wx.have(3);
            if(cursor_h.moveToFirst()){
                do {
                    String pack = cursor_h.getString(cursor_h.getColumnIndex("apps_pack"));
                    pack_name_have_timer.add(pack);

                    for(int have = 0; have < pack_name_have_timer.size(); have++){
                        Log.v("data","have timer block --------------------->"+pack_name_have_timer);
                        if(source.getPackageName().equals(pack_name_have_timer.get(have))){
                            Intent startMain = new Intent(Intent.ACTION_MAIN);
                            startMain.addCategory(Intent.CATEGORY_HOME);
                            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(startMain);

                            startService(new Intent(this, alert.class));
                        }
                    }
                }while (cursor_h.moveToNext());
            }cursor_h.close();



            for(int g = 0; g < block_list_app_child.size(); g++){
                if(source.getPackageName().equals(block_list_app_child.get(g))){
                    Log.v("data","true-------------------------------------->"+block_list_app_child.get(g));
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(startMain);

                    startService(new Intent(this, alert.class));
                }else{
                    Log.v("data","false----------------------l---------------->"+source.getPackageName());
                }
            }


            for(int adult = 0;adult < app_adult.size(); adult++){
                if(source.getPackageName().equals(app_adult.get(adult))){

                    Intent startHome = new Intent(Intent.ACTION_MAIN);
                    startHome.addCategory(Intent.CATEGORY_HOME);
                    startHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startHome);

                    startService(new Intent(this,alert.class));

                }
            }

            if(source.getPackageName().equals("com.android.chrome")){

                stopService(new Intent(this,alert.class));

                if(AccessibilityEvent.eventTypeToString(event.getEventType()).contains("WINDOW") && currentNode != null){

                    List<AccessibilityNodeInfo> search = currentNode.findAccessibilityNodeInfosByViewId("com.android.chrome:id/url_bar");

                    if(search.size()>0){

                        AccessibilityNodeInfo s = search.get(0);

                        if(s.getText() != null && s.getText().toString().length()>0){

                            CharSequence search_data = s.getText().toString();

                            String search_string = "Search or type web address";
                            String urla1 = s.getText().toString();

                            for(int ui = 0 ; ui < websites.size(); ui++){
                                if(urla1.contains(websites.get(ui))){
                                    Log.v("ito ang data","ng website"+search_data+" ===>class  "+currentNode.getClassName());
                                    startService(new Intent(this,block.class));
                                }
                            }
                            /*
                            if(websites.contains(search_data)){
                                Log.v("ito ang data","ng website"+search_data+" ===>class  "+currentNode.getClassName());
                                startService(new Intent(this,block.class));
                            } else{
                                //ito ang isasave sa database kasi hindi ito naka block
                                stopService(new Intent(this,block.class));

                                Log.v("current","date-------------------------------------->"+date);
                                Log.v("hindi ito ang data"," haha "+search_data);
                                //startActivity(new Intent(this,block.class));
                                String fucking_site = search_data.toString();


                                int count_history_sites = wx.checkifSameHistorySite(fucking_site,date);
                                if(count_history_sites != 0){
                                    Log.v("data","meron-----------------xxxxxxxxxxxxxxxxxxx");
                                }else{
                                    Log.v("data","wala------------------xxxxxxxxxxxxxxxxxxx");

                                    long id_history = wx.insertHistory_domain(fucking_site,date,parentid,childid);
                                    if(id_history>=0){
                                        Log.v("data","save successsfully--------------------->site");
                                    }else{
                                        Log.v("data","don't give up fuckers---------------------->");
                                    }
                                }
                            }
                            */

                            for(int v = 0; v < b_block.size(); v++){
                                if(b_block.get(v).equals(search_data)){
                                    startService(new Intent(this,block.class));
                                }
                            }



                            String urla = s.getText().toString();
                            String cslx = "https://";
                            String cslx1 = "http://";

                            if(block_list_web_parent.contains(search_data)){
                                startService(new Intent(this,block.class));
                            }
                            for(int op = 0; op < block_list_web_parent.size(); op++){
                                if(urla.contains(block_list_web_parent.get(op))){
                                    startService(new Intent(this,block.class));
                                }
                            }


                            /*
                            if(urla.contains(cslx)){
                                Log.v("website ","ito");
                                domain_name d = new domain_name();

                                try{

                                    CharSequence lax = d.getDomain(urla);
                                    CharSequence luxury = d.getAuthority(urla);


                                    Log.v("data","websites---------------------------->"+block_list_web_parent);
                                    if(block_list_web_parent.contains(lax)){
                                        Log.v("data","-------------------------------------"+lax);
                                    }else{
                                        Log.v("data","-------------------------------------"+lax);
                                    }

                                    //Log.v("data","websites--------->"+websites);

                                    if(websites.contains(lax) || websites.contains(luxury) || block_list_web_parent.contains(lax) ){

                                        startService(new Intent(this,block.class));
                                        Log.v("pota "," naman "+lax);

                                    }else if(websites.contains(urla)){
                                        startService(new Intent(this,block.class));
                                        Log.v("website"," true----------------------> "+urla);
                                    }else if(block_list_web_parent.equals(lax)){
                                        Log.v("true","website block ---------------->"+urla);
                                        startService(new Intent(this,block.class));
                                    }
                                    else{
                                        Log.v("data","hindihindihindihindihindihindihindihindihindihindihindihindihindi");
                                        stopService(new Intent(this,block.class));
                                    }
                                }catch(URISyntaxException e){
                                    e.printStackTrace();
                                }
                            }else{
                                Log.v("data","hindi website eeeeeeeeeeee");
                            }
                            */

                        }else{
                            stopService(new Intent(this,block.class));
                        }
                    }else{
                        stopService(new Intent(this,block.class));
                    }
                }
            }else{
                stopService(new Intent(this,block.class));
            }

        }
    }

    //installed apps scan
    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list){

        ArrayList<ApplicationInfo> applist = new ArrayList<>();

        for(ApplicationInfo info : list){
            try{
                if (null != packageManager.getLaunchIntentForPackage(info.packageName) && !info.packageName.equals("com.example.childapp")){
                    applist.add(info);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return applist;
    }

    public ArrayList<String> xinstalledapps(){

        ArrayList<String> data = new ArrayList<>();
        applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
        for(int x = 0; x < applist.size(); x++){
            ApplicationInfo info = applist.get(x);
            if(null != info){

                data.add(packageManager.getApplicationLabel(info).toString());

            }
        }
        return data;

    }

    public ArrayList<String> checkifnakainstalled(String appname){
        ArrayList<String> data = new ArrayList<>();
        applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
        for(int x = 0; x < applist.size(); x++){
            ApplicationInfo info = applist.get(x);
            if(null != info && appname.equals(info.packageName)){
                Log.v("data","app Name ------------------>"+packageManager.getApplicationLabel(info).toString());
                //Log.v("data"," installed--------------->"+appname);
                data.add(packageManager.getApplicationLabel(info).toString());
                //saved to database
            }
        }
        return data;
    }

    public ArrayList<Integer> getSessionId(Context context){
        websiteAdapter db = new websiteAdapter(context);

        ArrayList<Integer> data_child = db.getchildIdsession();
        //Log.v("data","session id child ------------------>"+data_child);
        return data_child;
    }

    //check if the array changed
    public int getarrayChanged(){
        websiteAdapter db = new websiteAdapter(MyService.this);
        ArrayList<Integer> getsize = db.checkifthesizechanged(parentid,childid);
        return getsize.size();
    }

    //check if same app
    public boolean checkSameNameApp(int parentid,int childid,String appname){
        websiteAdapter db = new websiteAdapter(MyService.this);
        int count = db.getidsameinstalled(parentid,childid,appname);
        boolean count_val;
        if(count!=0){
            count_val=true;
        }else{
            count_val=false;
        }
        return count_val;
    }

    //save the installed apps to database
    public void saveInstalled(int parentid,int childid,String appname){
        websiteAdapter db = new websiteAdapter(MyService.this);
        //Toast.makeText(MyService.this,"Please wait scanning.........",Toast.LENGTH_SHORT).show();
        applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
        for(int x = 0; x < applist.size(); x++){
            ApplicationInfo info = applist.get(x);
            if(null != info && packageManager.getApplicationLabel(info).toString().equals(appname)){

               // Log.v("package"," ng app-----------------> "+info.packageName);
               // Log.v("pangalan "," app------------------> "+packageManager.getApplicationLabel(info).toString());
                //saved to database
                long id = db.insertInstalledDataChild(info.packageName,packageManager.getApplicationLabel(info).toString(),parentid,childid,applist.size());

                if(id>=0){
                    //Toast.makeText(MyService.this,"Scanning Complete.........",Toast.LENGTH_SHORT).show();
                   // Log.v("data","installed apps childs----------------------> save");
                }else{
                   // Log.v("data","installed apps childs----------------------> not");
                }


            }
        }
        //Toast.makeText(MyService.this,"Scanning is Complete",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
