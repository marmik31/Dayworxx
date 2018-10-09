package com.project.dayworx.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ubuntu on 27/4/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper sInstance;

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, IDatabase.DATABASE_NAME, null, IDatabase.DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "create new database");
        try {
            db.execSQL(IDatabase.IMap.CREATE_TABLE_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onUpgrade || oldVersion = " + oldVersion + " || newVersion = " + newVersion);
    }
}
