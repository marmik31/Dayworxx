package com.project.dayworx.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.project.dayworx.database.IDatabase.IMap;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ubuntu on 27/4/16.
 */
public class DBAdapter {
    private static final String TAG = DBAdapter.class.getSimpleName();

    public static void insertUpdateMap(Context context, String key, String value) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(IMap.KEY_MAP_VALUE, value);

        Cursor cursor = db.query(IMap.TABLE_MAP, new String[]{IMap.KEY_ID}, IMap.KEY_MAP_KEY + " = '" + key + "'", null, null, null, null, null);
        int index = -1;
        if (cursor != null && cursor.getCount() > 0) { //if the row exist then return the id
            cursor.moveToFirst();
            index = cursor.getInt(cursor.getColumnIndex(IMap.KEY_ID));
            cursor.close();
        }

        if (index == -1) {
            contentValues.put(IMap.KEY_MAP_KEY, key);
            db.insert(IMap.TABLE_MAP, null, contentValues);
        } else {
            db.update(IMap.TABLE_MAP, contentValues, IMap.KEY_ID + " = '" + index + "'", null);
        }
        Log.d(TAG, "insertUpdateMap key = " + key + ", value = " + value);
    }

    public static String getMapKeyValueString(Context context, String key) {
        String value = null;
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + IMap.TABLE_MAP + " where " + IMap.KEY_MAP_KEY + " = '" + key + "'", null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            value = cursor.getString(cursor.getColumnIndexOrThrow(IMap.KEY_MAP_VALUE));
            cursor.close(); // that's important too, otherwise you're gonna leak cursors
        }
        Log.d(TAG, "getMapKeyValueString key = " + key + ", value = " + value);
        return value;
    }

    public static boolean getMapKeyValueBoolean(Context context, String key) {
        boolean result = false;
        if (IMap.TRUE.equalsIgnoreCase(getMapKeyValueString(context, key))) {
            result = true;
        }
        Log.d(TAG, "getMapKeyValueBoolean value [" + key + "] = " + result);
        return result;
    }

    public static void setMapKeyValueBoolean(Context context, String key,
                                             boolean value) {
        Log.d(TAG, "setMapKeyValueBoolean value[" + key + "] = " + value);
        if (value) {
            insertUpdateMap(context, key, IMap.TRUE);
        } else {
            insertUpdateMap(context, key, IMap.FALSE);
        }
    }

  
}
