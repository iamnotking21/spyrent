package com.ax.childapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class websiteAdapter {
    myWebsiteHelper mywebHelper;

    public String[] columnsnulltimer = {myWebsiteHelper.P_UID,myWebsiteHelper.UID,myWebsiteHelper.LoginIDParent,myWebsiteHelper.LoginIDChild,myWebsiteHelper.app_name,myWebsiteHelper.pack_name};
    public String[] columns1 = {myWebsiteHelper.P_UID,myWebsiteHelper.UID, myWebsiteHelper.LoginIDParent, myWebsiteHelper.LoginIDChild, myWebsiteHelper.oras, myWebsiteHelper.mins, myWebsiteHelper.timeset, myWebsiteHelper.soras, myWebsiteHelper.smins, myWebsiteHelper.stimeset, myWebsiteHelper.app_name, myWebsiteHelper.pack_name};
    public String[] columns = {myWebsiteHelper.UID,myWebsiteHelper.LoginIDChild,myWebsiteHelper.LoginIDParent,myWebsiteHelper.status,myWebsiteHelper.domain};
    public String[] columns_block_list = {myWebsiteHelper.P_UID,myWebsiteHelper.UID,myWebsiteHelper.LoginIDChild,myWebsiteHelper.LoginIDParent,myWebsiteHelper.status,myWebsiteHelper.domain};
    public String[] columns_childs_installed = {myWebsiteHelper.UID,myWebsiteHelper.LoginIDParent,myWebsiteHelper.LoginIDChild,myWebsiteHelper.size_app_child,myWebsiteHelper.app_name,myWebsiteHelper.pack_name};
    public String[] columns_history_domain = {myWebsiteHelper.UID,myWebsiteHelper.domain,myWebsiteHelper.current_dates,myWebsiteHelper.LoginIDParent,myWebsiteHelper.LoginIDChild};
    public String[] columns_history_domain_app = {myWebsiteHelper.UID,myWebsiteHelper.app_name,myWebsiteHelper.current_dates,myWebsiteHelper.LoginIDParent,myWebsiteHelper.LoginIDChild};
    public String[] columns_mobile_parent = {myWebsiteHelper.UID,myWebsiteHelper.mobile_number,myWebsiteHelper.LoginIDChild};


    public websiteAdapter(Context context){
        mywebHelper = new myWebsiteHelper(context);
    }

    //parent insert


    public long insertHistory_domain(String domain,String current_dates,int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.domain,domain);
        contentValues.put(myWebsiteHelper.current_dates,current_dates);
        contentValues.put(myWebsiteHelper.LoginIDParent,parentid);
        contentValues.put(myWebsiteHelper.LoginIDChild,childid);

        long id = db.insert(myWebsiteHelper.Table_name_history,null,contentValues);
        return id;
    }

    public int checkifSameHistorySite(String domain,String current_dates){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionArgs = {domain,current_dates};
        Cursor cursor = db.query(myWebsiteHelper.Table_name_history,columns_history_domain,myWebsiteHelper.domain+"=? and "+myWebsiteHelper.current_dates+"=?",selectionArgs,null,null,null,null);
        return cursor.getCount();

    }

    public long insertHistory_app(String appname,String current_dates,int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.app_name,appname);
        contentValues.put(myWebsiteHelper.current_dates,current_dates);
        contentValues.put(myWebsiteHelper.LoginIDParent,parentid);
        contentValues.put(myWebsiteHelper.LoginIDChild,childid);

        long id  = db.insert(myWebsiteHelper.Table_name_history_app,null,contentValues);
        return id;
    }

    public int checkIfsameHistoryApp(String appname,String current_dates){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionArgs = {appname,current_dates};
        Cursor cursor = db.query(myWebsiteHelper.Table_name_history_app,columns_history_domain_app,myWebsiteHelper.app_name+"=? and "+myWebsiteHelper.current_dates+"=?",selectionArgs,null,null,null,null);
        return cursor.getCount();
    }


    public long insertInstalledDataChild(String appname,String packname,int parentid,int childid,int size){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.LoginIDParent,parentid);
        contentValues.put(myWebsiteHelper.LoginIDChild,childid);
        contentValues.put(myWebsiteHelper.size_app_child,size);
        contentValues.put(myWebsiteHelper.app_name,appname);
        contentValues.put(myWebsiteHelper.pack_name,packname);

        long id = db.insert(myWebsiteHelper.Table_name_installed_apps, null,contentValues);
        return id;
    }

    //child insert
    public long insertDomainBlocklist(int parentid, int childid,String domain,int status){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.LoginIDParent,parentid);
        contentValues.put(myWebsiteHelper.LoginIDChild,childid);
        contentValues.put(myWebsiteHelper.domain,domain);
        contentValues.put(myWebsiteHelper.status,status);

        long id = db.insert(myWebsiteHelper.Table_on_block_web,null,contentValues);
        return id;

    }

    public long insertnulltimer(int parentid,int childid,String appname,String packname){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.LoginIDParent,parentid);
        contentValues.put(myWebsiteHelper.LoginIDChild,childid);
        contentValues.put(myWebsiteHelper.app_name,appname);
        contentValues.put(myWebsiteHelper.pack_name,packname);

        long id_count = db.insert(myWebsiteHelper.Table_name2,null,contentValues);
        return id_count;
    }


    public ArrayList<String> pack_name_have_timer(int timerstatus){

        SQLiteDatabase db = mywebHelper.getWritableDatabase();

        String[] selectArgs = {String.valueOf(timerstatus)};

        Cursor cursor = db.query(myWebsiteHelper.Table_name1,columns1,myWebsiteHelper.timerstatus+"=?",selectArgs,null,null,null,null);

        ArrayList<String> data = new ArrayList<>();

        if(cursor.moveToNext()){

            String pack_name = cursor.getString(cursor.getColumnIndex(myWebsiteHelper.pack_name));

            data.add(pack_name);

        }

        return data;

    }




    public int delete_unactive_domain(int uid,int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] select = {String.valueOf(uid),String.valueOf(parentid),String.valueOf(childid)};
        int count = db.delete(myWebsiteHelper.Table_on_block_web,myWebsiteHelper.UID+"=? and "+myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=?",select);
        return count;

    }

    public int delete_null_timer(int primary_key){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] select = {String.valueOf(primary_key)};
        int count = db.delete(myWebsiteHelper.Table_name2,myWebsiteHelper.LoginIDChild+"=?",select);
        return count;
    }

    public int countNullTimer(int primary_key){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        Cursor cursor = db.query(myWebsiteHelper.Table_name2,columnsnulltimer,null,null,null,null,null);
        return cursor.getCount();
    }

    public int delete_domain_block_list(int primary_key){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] select = {String.valueOf(primary_key)};
        int count = db.delete(myWebsiteHelper.Table_on_block_web,myWebsiteHelper.LoginIDChild+"=?",select);
        return count;
    }

    public int CountblockList(int primary_key){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        Cursor cursor = db.query(myWebsiteHelper.Table_on_block_web,columns_block_list,null,null,null,null,null,null);
        return cursor.getCount();
    }

    public int delete_block_app_have_timer(int primary_key){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] select = {String.valueOf(primary_key)};
        int count = db.delete(myWebsiteHelper.Table_name1,myWebsiteHelper.LoginIDChild+"=?",select);
        return count;
    }

    public int CounthaveTimer(int primary_key){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        Cursor cursor = db.query(myWebsiteHelper.Table_name1,columns1,null,null,null,null,null,null);
        return cursor.getCount();
    }

    public int delete_unactive_app(int uid, int parentid, int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] whereargs = {String.valueOf(parentid),String.valueOf(childid),String.valueOf(uid)};
        int count = db.delete(myWebsiteHelper.Table_name1,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=? and "+myWebsiteHelper.UID+"=?",whereargs);
        return count;
    }

    public int delete_unactive_null_timer(int uid,int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] whereargs = {String.valueOf(uid),String.valueOf(parentid),String.valueOf(childid)};
        int count = db.delete(myWebsiteHelper.Table_name2,myWebsiteHelper.UID+"=? and "+myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=? ",whereargs);
        return count;
    }

    public boolean fucking_delete_all(int parentid,int childid,int status){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        db.execSQL("delete from "+ myWebsiteHelper.Table_name1);
        return true;
    }

    public boolean fucking_delete_null_timer_all(int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        db.execSQL("delete from "+ myWebsiteHelper.Table_name2);
        return true;
    }

    public boolean delete_all_apps_installed(){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        db.delete(myWebsiteHelper.Table_name_installed_apps,null,null);
        return true;
    }



    public int update_block_app_active(int p_uid,int parentid,int childid,int timerstatus){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.timerstatus,timerstatus);

        String[] whereargs = {String.valueOf(p_uid),String.valueOf(parentid),String.valueOf(childid)};
        int count = db.update(myWebsiteHelper.Table_name1,contentValues,myWebsiteHelper.P_UID+"=? and "+myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=?",whereargs);
        return count;

    }



    public long insertData1(String pack_name,String app_name,int oras,int mins,int timeset,int soras,int smins, int stimeset,int loginidparent,int loginidchild,int status){
        SQLiteDatabase dbb = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //app & package name of app

        contentValues.put(myWebsiteHelper.app_name,app_name);
        contentValues.put(myWebsiteHelper.pack_name,pack_name);
        //end of timer
        contentValues.put(myWebsiteHelper.oras,oras);
        contentValues.put(myWebsiteHelper.mins,mins);
        contentValues.put(myWebsiteHelper.timeset,timeset);
        //start of timer
        contentValues.put(myWebsiteHelper.soras,soras);
        contentValues.put(myWebsiteHelper.smins,smins);
        contentValues.put(myWebsiteHelper.stimeset,stimeset);
        //parent and child login id
        contentValues.put(myWebsiteHelper.LoginIDParent,loginidparent);
        contentValues.put(myWebsiteHelper.LoginIDChild,loginidchild);
        //status =1
        contentValues.put(myWebsiteHelper.status,status);
        //event status =1
        long id = dbb.insert(myWebsiteHelper.Table_name1,null,contentValues);
        return id ;

    }

    public int getidsame(int loginidparent,int loginidchild,int status,String app_name){
        SQLiteDatabase dbb = mywebHelper.getWritableDatabase();
        String[] selectionargs = {String.valueOf(loginidparent),String.valueOf(loginidchild),String.valueOf(status),app_name};
        Cursor cursor = dbb.query(myWebsiteHelper.Table_name1,columns1,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=? and "+myWebsiteHelper.status+"=? and "+myWebsiteHelper.app_name+"=?",selectionargs,null,null,null,null);
        return cursor.getCount();
    }
    public int getidsameinstalled(int loginidparent,int loginidchild,String app_name){
        SQLiteDatabase dbb = mywebHelper.getWritableDatabase();
        String[] selectionargs = {String.valueOf(loginidparent),String.valueOf(loginidchild),app_name};
        Cursor cursor = dbb.query(myWebsiteHelper.Table_name_installed_apps,columns_childs_installed,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=? and "+myWebsiteHelper.pack_name+"=?",selectionargs,null,null,null,null);
        return cursor.getCount();
    }

    public int getcountrow(){
        SQLiteDatabase dbb = mywebHelper.getWritableDatabase();
        Cursor cursor = dbb.query(myWebsiteHelper.Table_name_installed_apps,columns_childs_installed,null,null,null,null,null,null);
        return cursor.getCount();
    }

    public int getidnulltimer(int parentid,int childid,String app_name){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionargs = {String.valueOf(parentid),String.valueOf(childid),app_name};
        Cursor cursor = db.query(myWebsiteHelper.Table_name2,columnsnulltimer,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=? and "+myWebsiteHelper.app_name+"=?",selectionargs,null,null,null,null);
        return cursor.getCount();
    }

    public int getiddomain(int parentid,int childid,String domain){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(childid),domain};
        Cursor cursor = db.query(myWebsiteHelper.Table_on_block_web,columns_block_list,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=? and "+myWebsiteHelper.domain+"=?",selectionArgs,null,null,null,null);
        return cursor.getCount();
    }

    public ArrayList<String> getapp_name(int loginidparent,int loginidchild,int status,String appname){
        SQLiteDatabase dbb = mywebHelper.getWritableDatabase();
        String[] selectionargs = {String.valueOf(loginidparent),String.valueOf(loginidchild),String.valueOf(status),appname};
        Cursor cursor = dbb.query(myWebsiteHelper.Table_name1,columns1,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=? and "+myWebsiteHelper.status+"=? and "+myWebsiteHelper.app_name+"=? ",selectionargs,null,null,null,null);
        ArrayList<String> data = new ArrayList<>();
        while(cursor.moveToNext()){
            String p_id = cursor.getString(cursor.getColumnIndex(myWebsiteHelper.app_name));
            data.add(p_id);
        }
        return data;
    }

    public ArrayList<String> checkifnakainstalled(int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionargs = {String.valueOf(parentid),String.valueOf(childid)};
        Cursor cursor = db.query(myWebsiteHelper.Table_name_installed_apps,columns_childs_installed,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=?",selectionargs,null,null,null,null);
        ArrayList<String> data = new ArrayList<>();
        while(cursor.moveToNext()){
            String appnamez = cursor.getString(cursor.getColumnIndex(myWebsiteHelper.pack_name));
            data.add(appnamez);
        }
        return data;
    }

    public ArrayList<Integer> checkifthesizechanged(int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionargs = {String.valueOf(parentid),String.valueOf(childid)};
        Cursor cursor = db.query(myWebsiteHelper.Table_name_installed_apps,columns_childs_installed,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=?",selectionargs,null,null,null,null);
        ArrayList<Integer> data = new ArrayList<>();
        while(cursor.moveToNext()){
            int appnamez = cursor.getInt(cursor.getColumnIndex(myWebsiteHelper.size_app_child));
            data.add(appnamez);
        }
        return data;
    }

    public Cursor fetch(int childid,int status){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionargs = {String.valueOf(childid),String.valueOf(status)};
        Cursor cursor = db.query(myWebsiteHelper.Table_name1,columns1,myWebsiteHelper.LoginIDChild+"=? and "+myWebsiteHelper.status+"=?",selectionargs,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetch_installed_apps_child(int parentid,int childid){
        SQLiteDatabase dbx = mywebHelper.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(childid)};
        Cursor cursor = dbx.query(myWebsiteHelper.Table_name_installed_apps,columns_childs_installed,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=?",selectionArgs,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetch_domain_history(int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(childid)};
        Cursor cursor = db.query(myWebsiteHelper.Table_name_history,columns_history_domain,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=?",selectionArgs,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetch_app_history(int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(childid)};
        Cursor cursor = db.query(myWebsiteHelper.Table_name_history_app,columns_history_domain_app,myWebsiteHelper.LoginIDParent+" =? and "+myWebsiteHelper.LoginIDChild+"=?",selectionArgs,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }


    public Cursor fetchnulltimer(){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        Cursor cursor = db.query(myWebsiteHelper.Table_name2,columnsnulltimer,null,null,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchHaveTimer(){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();

        Cursor cursor = db.query(myWebsiteHelper.Table_name1,columns1,null,null,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchdomain(){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        Cursor cursor = db.query(myWebsiteHelper.Table_on_block_web,columns_block_list,null,null,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public boolean DeletePostbyId(String app_name) {
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + myWebsiteHelper.Table_name_installed_apps + " WHERE " + myWebsiteHelper.pack_name + "!='" + app_name + "'");
        db.close();

        //db.delete(TABLE_CAT, CAT_ID + "= ?", new String[]{String.valueOf(catid)});
        return true;
    }

    public boolean DeleteChildAccounts() {
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + myWebsiteHelper.TABLE_NAME_2+" ");
        db.close();

        //db.delete(TABLE_CAT, CAT_ID + "= ?", new String[]{String.valueOf(catid)});
        return true;
    }

    public int getChildAccountCount(){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        Cursor cursor = db.query(myWebsiteHelper.TABLE_NAME_2,myWebsiteHelper.col_child,null,null,null,null,null);
        return cursor.getCount();
    }

    //getpassword

    public Cursor getChildAccountPass(int id){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(myWebsiteHelper.TABLE_NAME_2, new String[]{myWebsiteHelper.COL_C_3},myWebsiteHelper.COL_C_1+"=?",selectionArgs,null,null,null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }



    //childs account
    public int CountIDChild(int primary_key){
        SQLiteDatabase dbb = mywebHelper.getWritableDatabase();
        String[] selectionargs = {String.valueOf(primary_key)};
        Cursor cursor = dbb.query(myWebsiteHelper.TABLE_NAME_2,myWebsiteHelper.col_child,myWebsiteHelper.COL_C_1+"=?",selectionargs,null,null,null,null);
        return cursor.getCount();
    }

    public int logIdChild(String cname,String cpass){
        SQLiteDatabase dbb = mywebHelper.getWritableDatabase();
        String[] selectionargs = {cname,cpass,String.valueOf(1)};
        Cursor cursor = dbb.query(myWebsiteHelper.TABLE_NAME_2,myWebsiteHelper.col_child,myWebsiteHelper.COL_C_2+"=? and "+myWebsiteHelper.COL_C_3+"=? and "+myWebsiteHelper.statusx+"=?",selectionargs,null,null,null,null);
        return cursor.getCount();
    }

    public int getIdChild(String cname,String cpass){
        SQLiteDatabase dbb = mywebHelper.getWritableDatabase();
        String[] selectionargs = {cname,cpass,String.valueOf(1)};
        Cursor cursor = dbb.query(myWebsiteHelper.TABLE_NAME_2,myWebsiteHelper.col_child,myWebsiteHelper.COL_C_2+"=? and "+myWebsiteHelper.COL_C_3+"=? and "+myWebsiteHelper.statusx+"=?",selectionargs,null,null,null,null);
        int data = 0;
        while (cursor.moveToNext()){
            int id_child = cursor.getInt(cursor.getColumnIndex("ID"));
            data = id_child;
        }
        return data;
    }

    public long insertChildAccount(int primary_key,String cname,String cpass,int status_child,int parentid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.COL_C_1,primary_key);
        contentValues.put(myWebsiteHelper.COL_C_2,cname);
        contentValues.put(myWebsiteHelper.COL_C_3,cpass);
        contentValues.put(myWebsiteHelper.statusx,status_child);
        contentValues.put(myWebsiteHelper.parentid,parentid);

        long id = db.insert(myWebsiteHelper.TABLE_NAME_2,null,contentValues);

        return id;
    }


    //session table-------------------
    public int getParentId(int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(childid)};
        StringBuffer data = new StringBuffer();
        Cursor cursor =  db.query(myWebsiteHelper.TABLE_NAME_2,myWebsiteHelper.col_child,myWebsiteHelper.COL_C_1+"=?",selectionArgs,null,null,null,null);
        while (cursor.moveToNext()){
            int parentid = cursor.getInt(cursor.getColumnIndex("parentid"));
            data.append(parentid);
        }
        return Integer.parseInt(data.toString());
    }

    public long insertSessionID(int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.LoginIDParent,parentid);
        contentValues.put(myWebsiteHelper.LoginIDChild,childid);

        long id  = db.insert(myWebsiteHelper.session_table,null,contentValues);
        return id;

    }

    public int getCount(int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selectionArgs = {String.valueOf(parentid),String.valueOf(childid)};
        Cursor cursor = db.query(myWebsiteHelper.session_table,myWebsiteHelper.col_session_table,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=?",selectionArgs,null,null,null,null);
        return cursor.getCount();
    }

    public int delSessionTable(int parentid,int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] whereArgs = {String.valueOf(parentid),String.valueOf(childid)};
        int cursor = db.delete(myWebsiteHelper.session_table,myWebsiteHelper.LoginIDParent+"=? and "+myWebsiteHelper.LoginIDChild+"=?",whereArgs);
        return cursor;
    }

    public ArrayList<Integer> getchildIdsession(){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ArrayList<Integer> data = new ArrayList<>();
        Cursor cursor = db.query(myWebsiteHelper.session_table,myWebsiteHelper.col_session_table,null,null,null,null,null,null);
        while (cursor.moveToNext()){
            int childid = cursor.getInt(cursor.getColumnIndex(myWebsiteHelper.LoginIDChild));
            data.add(childid);
        }
        return data;
    }

    public ArrayList<Integer> getparentIDsession(){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ArrayList<Integer> data = new ArrayList<>();
        Cursor cursor = db.query(myWebsiteHelper.session_table,myWebsiteHelper.col_session_table,null,null,null,null,null,null);
        while (cursor.moveToNext()){
            int childid = cursor.getInt(cursor.getColumnIndex(myWebsiteHelper.LoginIDParent));
            data.add(childid);
        }
        return data;
    }

    public Cursor fetch_session() {
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        Cursor cursor = db.query(myWebsiteHelper.session_table,new String[]{myWebsiteHelper.LoginIDChild}, null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
    //mobile number -------------------
    public long insertParentNumber(String mobileNumber, int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.mobile_number,mobileNumber);
        contentValues.put(myWebsiteHelper.LoginIDChild,childid);

        long id = db.insert(myWebsiteHelper.mobile_num_parent_table,null,contentValues);
        return id;

    }

    public int getCountMobile(String mobilenumberx,int parentid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selection = {mobilenumberx,String.valueOf(parentid)};
        Cursor cursor = db.query(myWebsiteHelper.mobile_num_parent_table,columns_mobile_parent,myWebsiteHelper.mobile_number+"=? and "+myWebsiteHelper.LoginIDChild+"=?",selection,null,null,null,null);
        return cursor.getCount();
    }

    public Cursor getmobile_parent(int parentid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selection = {String.valueOf(parentid)};
        Cursor cursor = db.query(myWebsiteHelper.mobile_num_parent_table,columns_mobile_parent,myWebsiteHelper.LoginIDChild+"=?",selection,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //new function
    public Cursor fetchend(int stat){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selec = {String.valueOf(stat)};
        Cursor cursor = db.query(myWebsiteHelper.Table_name1,columns1,myWebsiteHelper.status+"=?",selec,null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void delete_have_timer(String app_name){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] select = {app_name};
        int count = db.delete(myWebsiteHelper.Table_name1,myWebsiteHelper.app_name+"=?",select);
    }


    public int update_block_app_active(String p_uid,int timerstatus){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(myWebsiteHelper.timerstatus,timerstatus);

        String[] whereargs = {p_uid};
        int count = db.update(myWebsiteHelper.Table_name1,contentValues,myWebsiteHelper.app_name+"=?",whereargs);
        return count;

    }

    public Cursor have(int timerStatus){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selec = {String.valueOf(timerStatus)};
        Cursor cursor = db.query(myWebsiteHelper.Table_name1,columns1,myWebsiteHelper.timerstatus+"=?",selec,null,null,null,null);
        if (cursor.moveToFirst()){
            cursor.moveToNext();
        }
        return cursor;
    }

    public String childname(int childid){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] s = {String.valueOf(childid)};
        StringBuffer data = new StringBuffer();
        Cursor cursor = db.query(myWebsiteHelper.session_table,myWebsiteHelper.col_session_table,myWebsiteHelper.LoginIDParent+"=?",s,null,null,null,null);
        while (cursor.moveToNext()){
            int child = cursor.getInt(cursor.getColumnIndex("loginIdchild"));
            data.append(child);
        }
        return data.toString();
    }

    public String pota_name(int id_child){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] data =  {String.valueOf(id_child)};
        StringBuffer f = new StringBuffer();
        Cursor cursor = db.query(myWebsiteHelper.TABLE_NAME_2,myWebsiteHelper.col_child,myWebsiteHelper.COL_C_1+"=?",data,null,null,null,null);
        while (cursor.moveToNext()){
            String cname = cursor.getString(cursor.getColumnIndex(myWebsiteHelper.COL_C_2));
            f.append(cname);
        }
        return f.toString();
    }

    public String pugeButial(int child){
        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] s = {String.valueOf(child)};
        StringBuffer data = new StringBuffer();
        Cursor cursor = db.query(myWebsiteHelper.TABLE_NAME_2,myWebsiteHelper.col_child,myWebsiteHelper.COL_C_1+"=?",s,null,null,null,null);
        while (cursor.moveToNext()){
            String uname = cursor.getString(cursor.getColumnIndex("CNAME"));
            data.append(uname);
        }
        return data.toString();
    }


    static class myWebsiteHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "childapp";
        //web table sa parent to pota

        //childs table blocking app

        public static final String TABLE_NAME_2 = "child";

        private static final String Table_name1 = "blocking_app_timer_child";
        private static final String Table_name2 = "nullTimer";

        private static final String Table_name_history_app = "app_history";

        //installed apps
        private static final String Table_name_installed_apps = "installed_apps_childs";

        //history
        private static final String Table_name_history = "history_domain";

        private static final String Table_on_block_web ="blocklist";

        private static final int DATABASE_Version =1;
        private static final String P_UID = "p_id";
        private static final String UID = "_id";

        private static final String LoginIDParent = "loginIdparent";
        private static final String LoginIDChild = "loginIdchild";
        private static final String status = "status";
        private static final String domain = "domain";

        //start
        private static final String oras = "oras";
        private static final String mins = "mins";
        private static final String timeset = "timeset";
        //end
        private static final String soras = "soras";
        private static final String smins = "smins";
        private static final String stimeset = "stimeset";
        //login id ng bata

        private static final String app_name = "apps_name";
        private static final String pack_name = "apps_pack";

        private static final String timerstatus = "timerstatus";
        private static final String size_app_child = "size";

        private static final String current_dates = "current_dates";
        private static final String app_status = "app_status";

        //child
        public static final String COL_C_1 = "ID";
        public static final String COL_C_2 = "CNAME";
        public static final String COL_C_3 = "CPASS";
        public static final String statusx = "status_child";
        public static final String parentid = "parentid";
        public static final String[] col_child = {COL_C_1,COL_C_2,COL_C_3,statusx,parentid};

        //-------------------------session table for child
        public static final String session_table = "session_table";

        public static final String[] col_session_table = {UID,LoginIDParent,LoginIDChild};

        //-----------------------mobile num parent
        public static final String mobile_num_parent_table = "mobile_num_parent_table";
        public static final String mobile_number = "number";


        private static final String Create_table1 = "CREATE TABLE "+Table_name1+" ("+P_UID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+
                UID+" INT(15),"+
                timerstatus+" INT(15),"+
                status+" INT(15),"+
                LoginIDParent+" INT(15),"+
                LoginIDChild+" INT(15), "+
                app_name+" VARCHAR(100),"+
                pack_name+" VARCHAR(100),"+
                oras+" VARCHAR(20),"+
                mins+" VARCHAR(20),"+
                timeset+" VARCHAR(20),"+
                soras+" VARCHAR(20),"+
                smins+" VARCHAR(20),"+
                stimeset+" VARCHAR(20)) ";

        private static final String Create_table_session_child = "CREATE TABLE "+session_table+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                LoginIDParent+" INT(20),"+
                LoginIDChild+" INT(20)"+
                ")";


        private static final String Create_table_mobile_parent = "CREATE TABLE "+mobile_num_parent_table+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                mobile_number+" INT(20),"+
                LoginIDChild+" INT(20)"+
                ")";



        private static final String Create_table_installed_apps = "CREATE TABLE "+Table_name_installed_apps+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                LoginIDParent+" INT(15),"+
                LoginIDChild+" INT(15),"+
                size_app_child+" INT(15),"+
                app_name+" VARCHAR(100),"+
                pack_name+" VARCHAR(100)"+")";


        private static final String Create_table_history = "CREATE TABLE "+Table_name_history+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+
                domain+" VARCHAR(100),"+
                current_dates+" VARCHAR(60),"+
                LoginIDParent+" INT(15),"+
                LoginIDChild+" INT(15)"+
                " ) ";

        private static final String Create_table_history_app = "CREATE TABLE "+Table_name_history_app+" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+
                app_name+" VARCHAR(100),"+
                current_dates+" VARCHAR(60),"+
                LoginIDParent+" INT(15),"+
                LoginIDChild+" INT(15)"+
                " ) ";


        private static final String Create_table_on_block_list = "CREATE TABLE "+Table_on_block_web+" ("+P_UID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+
                UID+" INT(15),"+
                domain+" VARCHAR(50),"+
                status+" INT(15),"+
                LoginIDParent+" INT(15),"+
                LoginIDChild+" INT(15)"+
                " ) ";

        private static final String Create_table2 = "CREATE TABLE "+Table_name2+" ("+P_UID+" INTEGER PRIMARY KEY AUTOINCREMENT,  "+
                UID+" INT(15),"+
                LoginIDParent+" INT(15),"+
                LoginIDChild+" INT(15),"+
                app_name+" VARCHAR(100),"+
                pack_name+" VARCHAR(100)"+
                " ) ";

        private static final String DROP_TABLE1 = "DROP TABLE IF EXISTS "+Table_name1+" ";
        private static final String DROP_TABLE2 = "DROP TABLE IF EXISTS "+Table_name2+" ";
        private static final String DROP_TABLE3 = "DROP TABLE IF EXISTS "+Table_on_block_web+" ";
        private static final String DROP_TABLE4 = "DROP TABLE IF EXISTS "+Table_name_installed_apps+" ";
        private static final String DROP_TABLE5 = "DROP TABLE IF EXISTS "+Table_name_history+" ";
        private static final String DROP_TABLE6 = "DROP TABLE IF EXISTS "+Table_name_history_app+" ";

        private static final String DROP_TABLE7 = "DROP TABLE IF EXISTS "+session_table+" ";
        private static final String DROP_TABLE8 = "DROP TABLE IF EXISTS "+mobile_num_parent_table+" ";


        private Context context;

        public myWebsiteHelper(Context context){
            super(context,DB_NAME,null,DATABASE_Version);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            try{
                sqLiteDatabase.execSQL(Create_table1);
                sqLiteDatabase.execSQL(Create_table2);
                sqLiteDatabase.execSQL(Create_table_on_block_list);
                sqLiteDatabase.execSQL(Create_table_installed_apps);
                sqLiteDatabase.execSQL(Create_table_history);
                sqLiteDatabase.execSQL(Create_table_history_app);

                sqLiteDatabase.execSQL(Create_table_session_child);
                sqLiteDatabase.execSQL(Create_table_mobile_parent);

                sqLiteDatabase.execSQL("create table "+ TABLE_NAME_2+" (ID INTEGER PRIMARY KEY AUTOINCREMENT,CNAME TEXT, CPASS TEXT, status_child INT(15),parentid INT(15))");


            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            try{
                Log.v("ito","ang upgrade");

                sqLiteDatabase.execSQL(DROP_TABLE1);
                sqLiteDatabase.execSQL(DROP_TABLE2);
                sqLiteDatabase.execSQL(DROP_TABLE3);
                sqLiteDatabase.execSQL(DROP_TABLE4);
                sqLiteDatabase.execSQL(DROP_TABLE5);
                sqLiteDatabase.execSQL(DROP_TABLE6);

                sqLiteDatabase.execSQL(DROP_TABLE7);
                sqLiteDatabase.execSQL(DROP_TABLE8);

                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_2);
                onCreate(sqLiteDatabase);
            }catch(Exception e){
                e.printStackTrace();
            }
        }





    }




    public int update_block_app_active(int p_uid,int timerstatus){

        SQLiteDatabase db = mywebHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();



        contentValues.put(myWebsiteHelper.timerstatus,timerstatus);



        String[] whereargs = {String.valueOf(p_uid)};

        int count = db.update(myWebsiteHelper.Table_name1,contentValues,myWebsiteHelper.P_UID+"=?",whereargs);

        return count;



    }

    public Cursor fetch(int status){

        SQLiteDatabase db = mywebHelper.getWritableDatabase();
        String[] selec = {String.valueOf(status)};
        Cursor cursor = db.query(myWebsiteHelper.Table_name1,columns1,myWebsiteHelper.status+"=?",selec,null,null,null,null);

        if(cursor != null){

            cursor.moveToFirst();

        }

        return cursor;

    }

}
