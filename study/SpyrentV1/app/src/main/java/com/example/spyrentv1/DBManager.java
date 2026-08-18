package com.example.spyrentv1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBManager {
    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper myDb;

    public DBManager(Context c){
        this.context = c;
    }

    public DBManager open() throws SQLException {
        this.myDb = new DatabaseHelper(this.context);
        this.database = this.myDb.getWritableDatabase();
        return this;
    }

    public Cursor fetch(int id) {
        StringBuffer data = new StringBuffer();
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = this.database.query(DatabaseHelper.TABLE_NAME, new String[]{ DatabaseHelper.COL_2, DatabaseHelper.COL_3, DatabaseHelper.COL_4}, DatabaseHelper.COL_1+"=?", selectionArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long id, String password) {
        ContentValues contentValues = new ContentValues();
        String[] selectionArgs = {String.valueOf(id)};
        contentValues.put(DatabaseHelper.COL_6, password);
        return this.database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.COL_1+"=?", selectionArgs);
    }

    public int update2(long id, String user, String fname, String lname, String numb, String password) {
        ContentValues contentValues = new ContentValues();
        String[] selectionArgs = {String.valueOf(id),user};

        contentValues.put(DatabaseHelper.COL_2, fname);
        contentValues.put(DatabaseHelper.COL_3, lname);
        contentValues.put(DatabaseHelper.COL_4, numb);
        contentValues.put(DatabaseHelper.COL_6, password);
        return this.database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.COL_1+"=? AND "+DatabaseHelper.COL_5+"=?", selectionArgs);
    }

    public int update_child_account(long id,String newPassword){
        ContentValues contentValues = new ContentValues();
        String[] selectionArgs = {String.valueOf(id)};
        contentValues.put(DatabaseHelper.COL_C_3,newPassword);
        return this.database.update(DatabaseHelper.TABLE_NAME_2, contentValues, DatabaseHelper.parentid+"=?", selectionArgs);
    }


    public void insert(int id) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.S_1, id);
        this.database.insert(DatabaseHelper.SESSION_TABLE, null, contentValue);
    }

    public void delete() {
        this.database.delete(DatabaseHelper.SESSION_TABLE,null,null);
            database.close();
    }

    public Cursor fetch_session() {
        Cursor cursor = this.database.query(DatabaseHelper.SESSION_TABLE, new String[]{DatabaseHelper.S_1}, null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetch_child(int id) {
        StringBuffer data = new StringBuffer();
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = this.database.query(DatabaseHelper.TABLE_NAME_2,DatabaseHelper.col_child, DatabaseHelper.parentid+"=?", selectionArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public ArrayList<String> fetch_child_arrayList(String child_name) {
        ArrayList<String> data = new ArrayList<>();
        String[] selectionArgs = {child_name};
        Cursor cursor = this.database.query(DatabaseHelper.TABLE_NAME_2,DatabaseHelper.col_child, DatabaseHelper.COL_C_2+"=?", selectionArgs, null, null, null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("ID"));
            data.add(name);
        }
        return data;
    }

}
