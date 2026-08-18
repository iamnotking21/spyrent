package com.example.spyrentv1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "spyrent.db";
    public static final String TABLE_NAME = "users";
    public static final String TABLE_NAME_2 = "child";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "FNAME";
    public static final String COL_3 = "LNAME";
    public static final String COL_4 = "EMAIL";
    public static final String COL_5 = "UNAME";
    public static final String COL_6 = "PASSWORD";
    public static final String COL_7 = "POS";
    public static final String[] col = {COL_1,COL_2,COL_3,COL_4,COL_5,COL_6,COL_7};

    public static final String SESSION_TABLE = "login_session";
    public static final String S_1 = "ID";

    public static final String COL_C_1 = "ID";
    public static final String COL_C_2 = "CNAME";
    public static final String COL_C_3 = "CPASS";
    public static final String status = "status_child";
    public static final String parentid = "parentid";
    public static final String[] col_child = {COL_C_1,COL_C_2,COL_C_3,status,parentid};

    //-----------------------------------websites
    private static final String Table_name = "webTable";
    private static final String UID = "_id";
    private static final String domain = "domain";
    private static final String LoginIDParent = "loginIdparent";
    private static final String LoginIDChild = "loginIdchild";
    public String[] columns = {UID,LoginIDChild,LoginIDParent,status,domain};

    //------------------------------------apps
    private static final String Table_name_apps = "lamesa1";
    //-----------------------------------walang timer
    private static final String Table_name2 = "walangtimer";
    private static final String eventstatus = "eventstatus";
    private static final String app_name = "apps_name";
    private static final String pack_name = "apps_pack";
    private static final String oras = "oras";
    private static final String mins = "mins";
    private static final String timeset = "timeset";
    //end
    private static final String soras = "soras";
    private static final String smins = "smins";
    private static final String stimeset = "stimeset";
    public String[] columns_apps = {UID, LoginIDParent, LoginIDChild,oras, mins, timeset, soras, smins, stimeset, app_name, pack_name,status};

    //-----------------------------------apps walang timer
    public String[] columnsTable_walangtimer = {UID,LoginIDParent,LoginIDChild,app_name,pack_name,eventstatus};

    //----------------------------------Login Session
    private static final String Create_Session_Table = "CREATE TABLE "+SESSION_TABLE+" ("+S_1+" INT(50))";
    //----------------------------------history app
    private static final String Table_name_history_app = "app_history";
    private static final String current_dates = "current_dates";
    public String[] columns_history_domain_app = {UID,app_name,current_dates,LoginIDParent,LoginIDChild};

    private static final String Create_table_history_app = "CREATE TABLE "+Table_name_history_app+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+
            app_name+" VARCHAR(100),"+
            current_dates+" VARCHAR(60),"+
            LoginIDParent+" INT(15),"+
            LoginIDChild+" INT(15)"+
            " ) ";

    //-------------------------------history domain sites

    private static final String Table_name_history = "history_domain";
    public String[] columns_history_domain = {UID,domain,current_dates,LoginIDParent,LoginIDChild};
    private static final String Create_table_history = "CREATE TABLE "+Table_name_history+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+
            domain+" VARCHAR(100),"+
            current_dates+" VARCHAR(60),"+
            LoginIDParent+" INT(15),"+
            LoginIDChild+" INT(15)"+
            " ) ";




    //------------------------------------apps
    private static final String Create_table_apps = "CREATE TABLE "+Table_name_apps+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+
            status+" INT(15),"+
            eventstatus+" INT(15),"+
            LoginIDParent+" INT(15),"+
            LoginIDChild+" INT(15), "+
            app_name+" VARCHAR(20),"+
            pack_name+" VARCHAR(20),"+
            oras+" VARCHAR(20),"+
            mins+" VARCHAR(20),"+
            timeset+" VARCHAR(20),"+
            soras+" VARCHAR(20),"+
            smins+" VARCHAR(20),"+
            stimeset+" VARCHAR(20)) ";

    private static final String Create_table2 = "CREATE TABLE "+Table_name2+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
            LoginIDParent+" INT(15),"+
            LoginIDChild+" INT(15),"+
            app_name+" VARCHAR(50),"+
            pack_name+" VARCHAR(50),"+
            eventstatus+" INT(15))";


    //---------------------------------------installed apps
    private static final String Table_name_installed_apps = "installed_apps_childs";
    public String[] columns_childs_installed = {UID,LoginIDParent,LoginIDChild,size_app_child,app_name,pack_name};
    private static final String size_app_child = "size";
    private static final String Create_table_installed_apps = "CREATE TABLE "+Table_name_installed_apps+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
            LoginIDParent+" INT(15),"+
            LoginIDChild+" INT(15),"+
            size_app_child+" INT(15),"+
            app_name+" VARCHAR(100),"+
            pack_name+" VARCHAR(100)"+")";



    //---------------------------------drop table if exists
    private static final String DROP_TABLE_WEBSITES = "DROP TABLE IF EXISTS "+Table_name+" ";
    private static final String DROP_TABLE_Apps = "DROP TABLE IF EXISTS "+Table_name_apps+" ";
    private static final String DROP_TABLE1 = "DROP TABLE IF EXISTS "+Table_name2+" ";

    private static final String DROP_TABLE6 = "DROP TABLE IF EXISTS "+Table_name_history_app+" ";
    private static final String DROP_TABLE5 = "DROP TABLE IF EXISTS "+Table_name_history+" ";

    private static final String DROP_TABLE4 = "DROP TABLE IF EXISTS "+Table_name_installed_apps+" ";
    private static final String DROP_SESSION_TABLE = "DROP TABLE IF EXISTS "+SESSION_TABLE+" ";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    private static final String Create_table_websites = "CREATE TABLE "+Table_name+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+
            domain+" VARCHAR(50),"+
            status+" INT(15),"+
            LoginIDParent+" INT(15),"+
            LoginIDChild+" INT(15)"+
            " ) ";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Create_table_websites);
        db.execSQL(Create_table_apps);
        db.execSQL(Create_table2);
        db.execSQL(Create_Session_Table);

        db.execSQL(Create_table_history_app);
        db.execSQL(Create_table_history);
        db.execSQL(Create_table_installed_apps);

        db.execSQL("create table "+ TABLE_NAME+" (ID INTEGER PRIMARY KEY AUTOINCREMENT,FNAME TEXT, LNAME TEXT, EMAIL TEXT, UNAME TEXT, PASSWORD TEXT, POS TEXT)");
        db.execSQL("create table "+ TABLE_NAME_2+" (ID INTEGER PRIMARY KEY AUTOINCREMENT,CNAME TEXT, CPASS TEXT, status_child INT(15),parentid INT(15))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_2);
        db.execSQL(DROP_TABLE1);
        db.execSQL(DROP_TABLE_WEBSITES);
        db.execSQL(DROP_TABLE_Apps);
        db.execSQL(DROP_SESSION_TABLE);

        db.execSQL(DROP_TABLE4);
        db.execSQL(DROP_TABLE6);
        db.execSQL(DROP_TABLE5);


        onCreate(db);
    }
    //------------------------------installed apps start

    public long insertInstalledDataChild(String appname,String packname,int parentid,int childid,int size){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(LoginIDParent,parentid);
        contentValues.put(LoginIDChild,childid);
        contentValues.put(size_app_child,size);
        contentValues.put(app_name,appname);
        contentValues.put(pack_name,packname);

        long id = db.insert(Table_name_installed_apps, null,contentValues);
        return id;
    }


    public int getidsameinstalled(int loginidparentx,int loginidchildx,String app_namex){
        SQLiteDatabase dbb = this.getWritableDatabase();
        String[] selectionargs = {String.valueOf(loginidparentx),String.valueOf(loginidchildx),app_namex};
        Cursor cursor = dbb.query(Table_name_installed_apps,columns_childs_installed,LoginIDParent+"=? and "+LoginIDChild+"=? and "+pack_name+"=?",selectionargs,null,null,null,null);
        return cursor.getCount();
    }


    public int getcountrow(){
        SQLiteDatabase dbb = this.getWritableDatabase();
        Cursor cursor = dbb.query(Table_name_installed_apps,columns_childs_installed,null,null,null,null,null,null);
        return cursor.getCount();
    }

    public void del_all_installed_apps(int parentid,int childid){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selection = {String.valueOf(parentid),String.valueOf(childid)};
        int count = db.delete(Table_name_installed_apps,LoginIDParent+" =? and "+LoginIDChild+"=?",selection);
    }


    public ArrayList<String> getapp_name(int parentid,int child){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(child)};
        Cursor cursor = db.query(Table_name_installed_apps,columns_childs_installed,LoginIDParent+"=? and "+LoginIDChild+"=?",selectionArgs,null,null,null,null);
        ArrayList<String> data = new ArrayList<>();
        while (cursor.moveToNext()){
            String domain_name = cursor.getString(cursor.getColumnIndex("apps_pack"));
            data.add(domain_name);
        }
        return data;
    }

    public ArrayList<String> getapp_packs(int parentid,int child){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(child)};
        Cursor cursor = db.query(Table_name_installed_apps,columns_childs_installed,LoginIDParent+"=? and "+LoginIDChild+"=?",selectionArgs,null,null,null,null);
        ArrayList<String> data = new ArrayList<>();
        while (cursor.moveToNext()){
            String domain_name = cursor.getString(cursor.getColumnIndex("apps_name"));
            data.add(domain_name);
        }
        return data;
    }

    //------------------------------installed apps end


    //-------------------------------history sites domain

    public long insertHistory_domain(String domainx,String current_datesx,int parentidx,int childidx){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(domain,domainx);
        contentValues.put(current_dates,current_datesx);
        contentValues.put(LoginIDParent,parentidx);
        contentValues.put(LoginIDChild,childidx);

        long id = db.insert(Table_name_history,null,contentValues);
        return id;
    }


    public int checkifSameHistorySite(String domainx,String current_datesx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {domainx,current_datesx};
        Cursor cursor = db.query(Table_name_history,columns_history_domain,domain+"=? and "+current_dates+"=?",selectionArgs,null,null,null,null);
        return cursor.getCount();

    }

    public Cursor fetch_domain_history(int parentidx,int childidx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx)};
        Cursor cursor = db.query(Table_name_history,columns_history_domain,LoginIDParent+"=? and "+LoginIDChild+"=?",selectionArgs,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public ArrayList<String> getdomainhistory(int parentid,int child){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(child)};
        Cursor cursor = db.query(Table_name_history,columns_history_domain,LoginIDParent+"=? and "+LoginIDChild+"=?",selectionArgs,null,null,null,null);
        ArrayList<String> data = new ArrayList<>();
        while (cursor.moveToNext()){
            String domain_name = cursor.getString(cursor.getColumnIndex("domain"));
            data.add(domain_name);
        }
        return data;
    }

    public ArrayList<String> getcurrenttimeDomainhistory(int parentid,int child){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(child)};
        Cursor cursor = db.query(Table_name_history,columns_history_domain,LoginIDParent+"=? and "+LoginIDChild+"=?",selectionArgs,null,null,null,null);
        ArrayList<String> data = new ArrayList<>();
        while (cursor.moveToNext()){
            String domain_name = cursor.getString(cursor.getColumnIndex("current_dates"));
            data.add(domain_name);
        }
        return data;
    }





    //-------------------------------end history sites domain



    //-------------------------------history app
    public long insertHistory_app(String appname,String current_datesx,int parentid,int childid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(app_name,appname);
        contentValues.put(current_dates,current_datesx);
        contentValues.put(LoginIDParent,parentid);
        contentValues.put(LoginIDChild,childid);

        long id  = db.insert(Table_name_history_app,null,contentValues);
        return id;
    }

    public int checkIfsameHistoryApp(String appnamex,String current_datesx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {appnamex,current_datesx};
        Cursor cursor = db.query(Table_name_history_app,columns_history_domain_app,app_name+"=? and "+current_dates+"=?",selectionArgs,null,null,null,null);
        return cursor.getCount();
    }


    public Cursor fetch_app_history(int parentidx,int childidx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx)};
        Cursor cursor = db.query(Table_name_history_app,columns_history_domain_app,LoginIDParent+" =? and "+LoginIDChild+"=?",selectionArgs,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void del_all_history_app(int parentid,int childid){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selection = {String.valueOf(parentid),String.valueOf(childid)};
        int count = db.delete(Table_name_history_app,LoginIDParent+" =? and "+LoginIDChild+"=?",selection);
    }

    public ArrayList<String> getapphistory(int parentid,int child){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(child)};
        Cursor cursor = db.query(Table_name_history_app,columns_history_domain_app,LoginIDParent+"=? and "+LoginIDChild+"=?",selectionArgs,null,null,null,null);
        ArrayList<String> data = new ArrayList<>();
        while (cursor.moveToNext()){
            String domain_name = cursor.getString(cursor.getColumnIndex("apps_name"));
            data.add(domain_name);
        }
        return data;
    }


    public ArrayList<String> getapphistoryCurrentTime(int parentid,int child){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(child)};
        Cursor cursor = db.query(Table_name_history_app,columns_history_domain_app,LoginIDParent+"=? and "+LoginIDChild+"=?",selectionArgs,null,null,null,null);
        ArrayList<String> data = new ArrayList<>();
        while (cursor.moveToNext()){
            String domain_name = cursor.getString(cursor.getColumnIndex("current_dates"));
            data.add(domain_name);
        }
        return data;
    }


    //-----------------------------end history app

    //insert domain
    public long insertData1(String pack_namex,String app_namex,int orasx,int minsx,int timesetx,int sorasx,int sminsx, int stimesetx,int loginidparentx,int loginidchildx,int statusx){
        SQLiteDatabase dbb = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //app & package name of app
        contentValues.put(app_name,app_namex);
        contentValues.put(pack_name,pack_namex);
        //end of timer
        contentValues.put(oras,orasx);
        contentValues.put(mins,minsx);
        contentValues.put(timeset,timesetx);
        //start of timer
        contentValues.put(soras,sorasx);
        contentValues.put(smins,sminsx);
        contentValues.put(stimeset,stimesetx);
        //parent and child login id
        contentValues.put(LoginIDParent,loginidparentx);
        contentValues.put(LoginIDChild,loginidchildx);
        //status =1
        contentValues.put(status,statusx);
        //event status =1
        long id = dbb.insert(Table_name_apps,null,contentValues);
        return id ;
    }

    public long insertData(String domainx,int statusx,int longidparentx,int longidchildx){
        SQLiteDatabase dbb = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(domain,domainx);
        contentValues.put(status,statusx);
        contentValues.put(LoginIDParent,longidparentx);
        contentValues.put(LoginIDChild,longidchildx);

        long id = dbb.insert(Table_name,null,contentValues);
        return id;
    }

    public int getCountweb(String domainx,int parentid,int childid,int statusx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(childid),String.valueOf(statusx),domainx};
        Cursor cursor = db.query(Table_name,columns,LoginIDParent+"=? and "+LoginIDChild+"=? and "+status+"=? and "+domain+"=?",selectionArgs,null,null,null);
        return cursor.getCount();
    }

    //-------------------domain
    public ArrayList<String> getAll(int parentidx,int childidx,int statusx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx),String.valueOf(statusx)};
        Cursor cursor = db.query(Table_name,columns,LoginIDParent+"=? and "+LoginIDChild+"=? and "+status+"=? ",selectionArgs,null,null,null,null);
        ArrayList<String> domain_data = new ArrayList<>();
        while(cursor.moveToNext()){
            String domain_name = cursor.getString(cursor.getColumnIndex(domain));
            domain_data.add(domain_name);
        }
        return domain_data;
    }

    public Cursor getAllUname(){
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME,col,null,null,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllChildAccounts(int statusx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] s = {String.valueOf(statusx)};
        Cursor cursor = db.query(TABLE_NAME_2,col_child,status+"=?",s,null,null,null);
        if(cursor != null ){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllwebTable(int statusx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(statusx)};
        Cursor cursor = db.query(Table_name,columns,status+"=?",selectionArgs,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllWalangTimer(int statusx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(statusx)};
        Cursor cursor =  db.query(Table_name2,columnsTable_walangtimer,eventstatus+" =? ",selectionArgs,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllHaveTimerApps(int statusx){
        SQLiteDatabase db =  this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(statusx)};
        Cursor cursor = db.query(Table_name_apps,columns_apps,status+"=?",selectionArgs,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public ArrayList<String> getAllID(int parentidx,int childidx,int statusx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx),String.valueOf(statusx)};
        Cursor cursor = db.query(Table_name,columns,LoginIDParent+"=? and "+LoginIDChild+"=? and "+status+"=? ",selectionArgs,null,null,null,null);
        ArrayList<String> domain_data = new ArrayList<>();
        while(cursor.moveToNext()){
            int webid = cursor.getInt(cursor.getColumnIndex(UID));
            domain_data.add(String.valueOf(webid));
        }
        return domain_data;
    }

    public int update_web_unstatus(int uidx,int parentidx,int childidx,int statusx){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(status,statusx);

        String[] whereargs = {String.valueOf(uidx),String.valueOf(parentidx),String.valueOf(childidx)};
        int count = db.update(Table_name,contentValues,UID+"=? and "+LoginIDParent+"=? and "+LoginIDChild+"=?",whereargs);
        return count;
    }

    public int update_web(int uidx,int parentidx,int childidx,int statusx,String domainx){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(domain,domainx);

        String[] whereargs = {String.valueOf(uidx),String.valueOf(parentidx),String.valueOf(childidx),String.valueOf(statusx)};
        int count = db.update(Table_name,contentValues,UID+"=? and "+LoginIDParent+"=? and "+LoginIDChild+"=? and "+status+"=? ",whereargs);
        return count;
    }


    //------------------domain

    //------------------apps


    public ArrayList<String> getappname(int parentidx,int childidx,int statusx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx),String.valueOf(statusx)};
        Cursor cursor = db.query(Table_name_apps,columns_apps,LoginIDParent+"=? and "+LoginIDChild+"=? and "+status+"=?",selectionArgs,null,null,null,null
        );
        ArrayList<String> bufferidx = new ArrayList<>();
        while(cursor.moveToNext()){
            String app_namex = cursor.getString(cursor.getColumnIndex(app_name));
            bufferidx.add(app_namex);
        }
        return bufferidx;
    }


    public ArrayList<String> getoras2(int parentidx,int childidx,String appnamex,int statusx){
        SQLiteDatabase db = this.getWritableDatabase();
        String am_pm1 = "";
        String am_pm2 = "";
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx),appnamex,String.valueOf(statusx)};
        Cursor cursor = db.query(Table_name_apps,columns_apps,LoginIDParent+"=? and "+LoginIDChild+"=? and "+app_name+"=? and "+status+"=?",selectionArgs,null,null,null,null);
        ArrayList<String> buffer = new ArrayList<>();
        while(cursor.moveToNext()){
            //end
            int oras = cursor.getInt(cursor.getColumnIndex("oras"));
            int mins = cursor.getInt(cursor.getColumnIndex("mins"));
            int format1 = cursor.getInt(cursor.getColumnIndex("timeset"));
            //start
            int soras = cursor.getInt(cursor.getColumnIndex("soras"));
            int smins = cursor.getInt(cursor.getColumnIndex("smins"));
            int format2 = cursor.getInt(cursor.getColumnIndex("stimeset"));

            if(format1==1){
                am_pm1 = "PM";
            }else{
                am_pm1 = "AM";
            }

            if(format2==1){
                am_pm2 = "PM";
            }else{
                am_pm2 = "AM";
            }

            buffer.add(soras+":"+smins+" "+am_pm2+"  "+oras+":"+mins+" "+am_pm1);
        }
        return buffer;
    }

    public ArrayList<String> getappnamenulltimer(int parentidx,int childidx,int eventstatusx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx),String.valueOf(eventstatusx)};
        Cursor cursor = db.query(Table_name2,columnsTable_walangtimer,LoginIDParent+"=? and "+LoginIDChild+"=? and "+eventstatus+"=?",selectionArgs,null,null,null,null);
        ArrayList<String> buffername = new ArrayList<>();
        while(cursor.moveToNext()){
            String app_name = cursor.getString(cursor.getColumnIndex("apps_name"));
            int uid = cursor.getInt(cursor.getColumnIndex("_id"));
            buffername.add(app_name);
        }
        return buffername;
    }

    public long insertnullTimer(int longidparentx,int longidchildx,String packnamex,String app_namex,int eventstatusx){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(LoginIDParent,longidparentx);
        contentValues.put(LoginIDChild,longidchildx);
        contentValues.put(pack_name,packnamex);
        contentValues.put(app_name,app_namex);
        contentValues.put(eventstatus,eventstatusx);

        long id = db.insert(Table_name2,null,contentValues);
        return id;

    }

    public int getData(int parentidx,int childidx,String apps_namex){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "loginIdchild="+childidx+" and loginIdparent="+parentidx;
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx),apps_namex};
        Cursor cursor = db.query(Table_name_apps,columns_apps,LoginIDParent+"=? and "+LoginIDChild+"=? and "+app_name+"=?",selectionArgs,null,null,null,null
        );
        return cursor.getCount();
    }

    public String getAll(int parentidx,int childidx){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx)};
        Cursor cursor = db.query(Table_name_apps,columns_apps,LoginIDParent+"=? and "+LoginIDChild+"=?",selectionArgs,null,null,null,null
        );
        StringBuffer bufferidx= new StringBuffer();
        while(cursor.moveToNext()){
            String app_name = cursor.getString(cursor.getColumnIndex("apps_name"));
            int app_uid = cursor.getInt(cursor.getColumnIndex("_id"));
            bufferidx.append(app_uid+"\n");
        }
        return bufferidx.toString();
    }

    public String getappuid(int parentidx,int childidx,String apps_namex){
        SQLiteDatabase db = this.getWritableDatabase();

        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx),apps_namex};
        Cursor cursor = db.query(Table_name_apps,columns_apps,LoginIDParent+"=? and "+LoginIDChild+"=? and "+app_name+"=?",selectionArgs,null,null,null,null
        );
        StringBuffer bufferidx= new StringBuffer();
        while(cursor.moveToNext()){
            String app_name = cursor.getString(cursor.getColumnIndex("apps_name"));
            int app_uid = cursor.getInt(cursor.getColumnIndex("_id"));
            bufferidx.append(app_uid);
        }
        return bufferidx.toString();
    }

    public int updateapp_pack(int uidx,String pack_namex,String app_namex,int orasx,int minsx,int timesetx,int sorasx,int sminsx, int stimesetx,int loginidparentx,int loginidchildx,int statusx){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //app & package name of app
        contentValues.put(app_name,app_namex);
        contentValues.put(pack_name,pack_namex);
        //end of timer
        contentValues.put(oras,orasx);
        contentValues.put(mins,minsx);
        contentValues.put(timeset,timesetx);
        //start of timer
        contentValues.put(soras,sorasx);
        contentValues.put(smins,sminsx);
        contentValues.put(stimeset,stimesetx);
        //parent and child login id
        contentValues.put(LoginIDParent,loginidparentx);
        contentValues.put(LoginIDChild,loginidchildx);
        //status = 1
        contentValues.put(status,statusx);

        String[] whereargs ={String.valueOf(uidx)};
        int count = db.update(Table_name_apps,contentValues,UID+"= ? ",whereargs);
        return count;
    }

    public ArrayList<String> getappnamenulltimer2(int parentidx,int childidx,int eventstatusx,String appnamex){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(childidx),String.valueOf(eventstatusx),appnamex};
        Cursor cursor = db.query(Table_name2,columnsTable_walangtimer,LoginIDParent+"=? and "+LoginIDChild+"=? and "+eventstatus+"=? and "+app_name+"=?",selectionArgs,null,null,null,null);
        ArrayList<String> buffername = new ArrayList<>();
        while(cursor.moveToNext()){
            String app_name = cursor.getString(cursor.getColumnIndex("apps_name"));
            int uid = cursor.getInt(cursor.getColumnIndex("_id"));
            buffername.add(String.valueOf(uid));
        }
        return buffername;
    }



    public int updatenulltimer(int longidparentx,int longidchildx,String app_namex,int eventstatusx,int uidx){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(eventstatus,eventstatusx);

        String[] whereargs = {String.valueOf(longidparentx),String.valueOf(longidchildx),app_namex,String.valueOf(uidx)};
        int count = db.update(Table_name2,contentValues,LoginIDParent+"=? and "+LoginIDChild+"=? and "+app_name+"=? and "+UID+"=?",whereargs);
        return count;
    }


    //-------------------apps

    public boolean insertD(String fname, String lname, String email, String uname, String password, String pos){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,fname);
        contentValues.put(COL_3,lname);
        contentValues.put(COL_4,email);
        contentValues.put(COL_5,uname);
        contentValues.put(COL_6,password);
        contentValues.put(COL_7,pos);
        long result = db.insert(TABLE_NAME,null,contentValues);
        if (result==-1)
            return false;
        else
            return true;

    }


    public boolean checker(String uname ,String password, String pos){
        String[] columns = { COL_1 };
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_5 + "=?" + " AND " + COL_6 + "=?"+ " AND " + COL_7 + "=?";
        String[] selectArgs =  { uname , password, pos };
        Cursor cursor = db.query(TABLE_NAME,columns,selection,selectArgs,null,null,null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        if (count>0)
            return true;
        else
            return false;
    }

    public int checkifsameUsernameandPassword(String uname,String pword){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {uname,pword};
        Cursor cursor = db.query(TABLE_NAME,col,COL_5+"=? AND "+COL_6+"=?",selectionArgs,null,null,null,null);
        return cursor.getCount();
    }

    public int check_if_same_username_child(String child_username){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] selectionArgs = {child_username};
        Cursor cursor = db.query(TABLE_NAME_2,col_child,COL_C_2+"=?",selectionArgs,null,null,null,null);
        return cursor.getCount();
    }

    public int update_username_child(String newUsername,int parentidx,int childid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_C_2,newUsername);

        String[] whereArgs = {String.valueOf(parentidx),String.valueOf(childid)};
        int count = db.update(TABLE_NAME_2,contentValues,parentid+"=? and "+COL_C_1+" =? ",whereArgs);
        return count;
    }

    //update the status of child to 2 unactive
    public int update_child_unactive(int unstatus,int parentidx,int childid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(status,unstatus);

        String[] whereArgs = {String.valueOf(parentidx),String.valueOf(childid)};
        int count = db.update(TABLE_NAME_2,contentValues,parentid+"=? and "+COL_C_1+"=?",whereArgs);
        return count;
    }

    public ArrayList<Integer> sessionid(String username_parent){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Integer> data = new ArrayList<>();
        String[] selectionArgs = {username_parent};
        Cursor cursor = db.query(TABLE_NAME,col,COL_5+"=? ",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex("ID"));
            data.add(id);
        }
        return data;
    }

    //fetch all data in childs table according to parentid
    public ArrayList<String> childs_name (int parentidx,int status_active){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> data = new ArrayList<>();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(status_active)};
        Cursor cursor = db.query(TABLE_NAME_2,col_child,parentid+"=? and "+status+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("CNAME"));
            data.add(name);
        }
        return data;
    }

    public ArrayList<String> childs_name_2 (String parentidx,int status_active){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> data = new ArrayList<>();
        String[] selectionArgs = {parentidx,String.valueOf(status_active)};
        Cursor cursor = db.query(TABLE_NAME_2,col_child,COL_C_2+"=? and "+status+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("CNAME"));
            data.add(name);
        }
        return data;
    }

    public ArrayList<String> data_name (int parentidx,int childx,int status_active){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> data = new ArrayList<>();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(status_active),String.valueOf(childx)};
        Cursor cursor = db.query(TABLE_NAME_2,col_child,parentid+"=? and "+status+"=? and "+COL_C_1+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("CNAME"));
            data.add(name);
        }
        return data;
    }

    public ArrayList<String> childs_id (int parentidx,int status_active){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> data = new ArrayList<>();
        String[] selectionArgs = {String.valueOf(parentidx),String.valueOf(status_active)};
        Cursor cursor = db.query(TABLE_NAME_2,col_child,parentid+"=? and "+status+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex("ID"));
            data.add(String.valueOf(id));
        }
        return data;
    }

    public ArrayList<String> childs_id_2 (String parentidx,int status_active){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> data = new ArrayList<>();
        String[] selectionArgs = {parentidx,String.valueOf(status_active)};
        Cursor cursor = db.query(TABLE_NAME_2,col_child,COL_C_2+"=? and "+status+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex("ID"));
            data.add(String.valueOf(id));
        }
        return data;
    }

    //parent_password pag kuha ng password ng parent para i insert sa child
    public String parent_password(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuffer data = new StringBuffer();
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(TABLE_NAME,col,COL_1+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            String password = cursor.getString(cursor.getColumnIndex("PASSWORD"));
            data.append(password);
        }
        return data.toString();
    }
    public String parent_username(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuffer data = new StringBuffer();
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(TABLE_NAME,col,COL_1+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            String usname = cursor.getString(cursor.getColumnIndex("UNAME"));
            data.append(usname);
        }
        return data.toString();
    }

    public String parent_name(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuffer data = new StringBuffer();
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(TABLE_NAME,col,COL_1+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            String password = cursor.getString(cursor.getColumnIndex("UNAME"));
            data.append(password);
        }
        return data.toString();
    }

    public String child_name(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuffer data = new StringBuffer();
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(TABLE_NAME_2,col_child,COL_C_1+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            String uname = cursor.getString(cursor.getColumnIndex("CNAME"));
            data.append(uname);
        }
        return data.toString();
    }

    public boolean insChild(String cname, String cpass,int statuss,int parentidx) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_C_2,cname);
        contentValues.put(COL_C_3,cpass);
        contentValues.put(status,statuss);
        contentValues.put(parentid,parentidx);

        long result = db.insert(TABLE_NAME_2,null,contentValues);
        boolean val;
        if(result >= 0){
            val = true;
        }else{
            val = false;
        }
        return  val;
    }

    public boolean check(String uname ,String email){
        String[] columns = { COL_1 };
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_5 + "=?" + " AND " + COL_4 + "=?";
        String[] selectArgs =  { uname , email};
        Cursor cursor = db.query(TABLE_NAME,columns,selection,selectArgs,null,null,null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        if (count>0)
            return true;
        else
            return false;
    }
}
