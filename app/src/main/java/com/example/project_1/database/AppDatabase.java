package com.example.project_1.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "teamkyutae.db";
    private static final int DATABASE_VERSION = 1;

    // 테이블 이름 정의
    public static final String TABLE_USERS = "users";
    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_CHAT_MESSAGES = "chat_messages";
    public static final String TABLE_USER_LOCATIONS = "user_locations";

    // Users 테이블 컬럼
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_AGREE = "agree";

    // Groups 테이블 컬럼
    public static final String COLUMN_GROUP_ID = "id";
    public static final String COLUMN_GROUP_NAME = "name";
    public static final String COLUMN_GROUP_LEADER_ID = "leader_id";

    // Chat Messages 테이블 컬럼
    public static final String COLUMN_MESSAGE_ID = "id";
    public static final String COLUMN_MESSAGE_GROUP_ID = "group_id";
    public static final String COLUMN_MESSAGE_USER_ID = "user_id";
    public static final String COLUMN_MESSAGE_CONTENT = "content";
    public static final String COLUMN_MESSAGE_TIMESTAMP = "timestamp";

    // User Locations 테이블 컬럼
    public static final String COLUMN_LOCATION_ID = "id";
    public static final String COLUMN_LOCATION_USER_ID = "user_id";
    public static final String COLUMN_LOCATION_LATITUDE = "latitude";
    public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    public static final String COLUMN_LOCATION_TIMESTAMP = "timestamp";

    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users 테이블 생성
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_USER_NAME + " TEXT NOT NULL, " +
                COLUMN_USER_AGREE + " INTEGER DEFAULT 0)";
        db.execSQL(createUsersTable);

        // Groups 테이블 생성
        String createGroupsTable = "CREATE TABLE " + TABLE_GROUPS + " (" +
                COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GROUP_NAME + " TEXT NOT NULL, " +
                COLUMN_GROUP_LEADER_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_GROUP_LEADER_ID + ") REFERENCES " + 
                TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        db.execSQL(createGroupsTable);

        // Chat Messages 테이블 생성
        String createChatMessagesTable = "CREATE TABLE " + TABLE_CHAT_MESSAGES + " (" +
                COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MESSAGE_GROUP_ID + " INTEGER, " +
                COLUMN_MESSAGE_USER_ID + " INTEGER, " +
                COLUMN_MESSAGE_CONTENT + " TEXT NOT NULL, " +
                COLUMN_MESSAGE_TIMESTAMP + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_MESSAGE_GROUP_ID + ") REFERENCES " + 
                TABLE_GROUPS + "(" + COLUMN_GROUP_ID + "), " +
                "FOREIGN KEY(" + COLUMN_MESSAGE_USER_ID + ") REFERENCES " + 
                TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        db.execSQL(createChatMessagesTable);

        // User Locations 테이블 생성
        String createUserLocationsTable = "CREATE TABLE " + TABLE_USER_LOCATIONS + " (" +
                COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LOCATION_USER_ID + " INTEGER, " +
                COLUMN_LOCATION_LATITUDE + " REAL, " +
                COLUMN_LOCATION_LONGITUDE + " REAL, " +
                COLUMN_LOCATION_TIMESTAMP + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_LOCATION_USER_ID + ") REFERENCES " + 
                TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        db.execSQL(createUserLocationsTable);

        // 테스트 데이터 삽입
        insertTestData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스 업그레이드 시 처리
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void insertTestData(SQLiteDatabase db) {
        // 테스트 사용자 데이터 삽입
        String[] testUsers = {
            "INSERT INTO " + TABLE_USERS + " (" + COLUMN_USER_ID + ", " + COLUMN_USER_NAME + ", " + COLUMN_USER_AGREE + ") VALUES (100001, '김철수', 1)",
            "INSERT INTO " + TABLE_USERS + " (" + COLUMN_USER_ID + ", " + COLUMN_USER_NAME + ", " + COLUMN_USER_AGREE + ") VALUES (100002, '이영희', 1)",
            "INSERT INTO " + TABLE_USERS + " (" + COLUMN_USER_ID + ", " + COLUMN_USER_NAME + ", " + COLUMN_USER_AGREE + ") VALUES (100003, '박지민', 0)"
        };

        for (String query : testUsers) {
            db.execSQL(query);
        }
    }
} 