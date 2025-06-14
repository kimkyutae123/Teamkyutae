package com.example.project_1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class ServerProvider extends ContentProvider {
    private static final String TAG = "ServerProvider";
    private static final String AUTHORITY = "com.example.project_1.serverprovider";
    private static final String SERVER_TABLE = "server_status";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SERVER_TABLE);
    
    private static final int SERVER_STATUS = 1;
    private static final UriMatcher uriMatcher;
    
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, SERVER_TABLE, SERVER_STATUS);
    }
    
    private SQLiteOpenHelper dbHelper;
    
    @Override
    public boolean onCreate() {
        dbHelper = new SQLiteOpenHelper(getContext(), "server.db", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE " + SERVER_TABLE + " (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "is_server INTEGER," +
                        "server_ip TEXT," +
                        "timestamp INTEGER" +
                        ")");
            }
            
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + SERVER_TABLE);
                onCreate(db);
            }
        };
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        switch (uriMatcher.match(uri)) {
            case SERVER_STATUS:
                cursor = db.query(SERVER_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SERVER_STATUS:
                return "vnd.android.cursor.dir/vnd.com.example.project_1.server";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        
        switch (uriMatcher.match(uri)) {
            case SERVER_STATUS:
                // 기존 데이터 삭제
                db.delete(SERVER_TABLE, null, null);
                // 새로운 데이터 삽입
                id = db.insert(SERVER_TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, String.valueOf(id));
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        
        switch (uriMatcher.match(uri)) {
            case SERVER_STATUS:
                count = db.delete(SERVER_TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        
        switch (uriMatcher.match(uri)) {
            case SERVER_STATUS:
                count = db.update(SERVER_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
} 