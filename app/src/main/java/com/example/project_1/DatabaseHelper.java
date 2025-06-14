package com.example.project_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TeamKyutae.db";
    private static final int DATABASE_VERSION = 1;

    // 사용자 테이블
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_USER_AGREED = "user_agreed";

    // 그룹 테이블
    public static final String TABLE_GROUPS = "groups";
    public static final String COLUMN_GROUP_ID = "id";
    public static final String COLUMN_GROUP_NAME = "name";
    public static final String COLUMN_GROUP_DESCRIPTION = "description";
    public static final String COLUMN_GROUP_LEADER_ID = "leader_id";
    public static final String COLUMN_GROUP_CREATED_AT = "created_at";

    // 멤버 테이블
    public static final String TABLE_MEMBERS = "members";
    public static final String COLUMN_MEMBER_ID = "id";
    public static final String COLUMN_MEMBER_GROUP_ID = "group_id";
    public static final String COLUMN_MEMBER_USER_ID = "user_id";
    public static final String COLUMN_MEMBER_JOINED_AT = "joined_at";
    public static final String COLUMN_MEMBER_NAME = "name";
    public static final String COLUMN_MEMBER_PHONE = "phone";

    // 채팅 테이블
    public static final String TABLE_CHATS = "chats";
    public static final String COLUMN_CHAT_ID = "id";
    public static final String COLUMN_CHAT_GROUP_ID = "group_id";
    public static final String COLUMN_CHAT_USER_ID = "user_id";
    public static final String COLUMN_CHAT_MESSAGE = "message";
    public static final String COLUMN_CHAT_CREATED_AT = "created_at";

    // 위치 정보 테이블
    public static final String TABLE_LOCATIONS = "locations";
    public static final String COLUMN_LOCATION_ID = "id";
    public static final String COLUMN_LOCATION_USER_ID = "user_id";
    public static final String COLUMN_LOCATION_LATITUDE = "latitude";
    public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    public static final String COLUMN_LOCATION_UPDATED_AT = "updated_at";

    private static final String TAG = "DatabaseHelper";

    // 사용자 테이블 생성 쿼리
    private static final String CREATE_USERS_TABLE = 
        "CREATE TABLE " + TABLE_USERS + " (" +
        COLUMN_USER_ID + " INTEGER PRIMARY KEY, " +
        COLUMN_USER_NAME + " TEXT NOT NULL, " +
        COLUMN_USER_AGREED + " INTEGER DEFAULT 0" +
        ")";

    // 그룹 테이블 생성 쿼리
    private static final String CREATE_GROUPS_TABLE = 
        "CREATE TABLE " + TABLE_GROUPS + " (" +
        COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_GROUP_NAME + " TEXT NOT NULL, " +
        COLUMN_GROUP_DESCRIPTION + " TEXT, " +
        COLUMN_GROUP_LEADER_ID + " INTEGER, " +
        COLUMN_GROUP_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
        "FOREIGN KEY(" + COLUMN_GROUP_LEADER_ID + ") REFERENCES " + 
        TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
        ")";

    // 멤버 테이블 생성 쿼리
    private static final String CREATE_MEMBERS_TABLE = 
        "CREATE TABLE " + TABLE_MEMBERS + " (" +
        COLUMN_MEMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_MEMBER_GROUP_ID + " INTEGER, " +
        COLUMN_MEMBER_USER_ID + " INTEGER, " +
        COLUMN_MEMBER_JOINED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_MEMBER_NAME + " TEXT, " +
        COLUMN_MEMBER_PHONE + " TEXT, " +
        "FOREIGN KEY(" + COLUMN_MEMBER_GROUP_ID + ") REFERENCES " + 
        TABLE_GROUPS + "(" + COLUMN_GROUP_ID + "), " +
        "FOREIGN KEY(" + COLUMN_MEMBER_USER_ID + ") REFERENCES " + 
        TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
        ")";

    // 채팅 테이블 생성 쿼리
    private static final String CREATE_CHATS_TABLE = 
        "CREATE TABLE " + TABLE_CHATS + " (" +
        COLUMN_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_CHAT_GROUP_ID + " INTEGER, " +
        COLUMN_CHAT_USER_ID + " INTEGER, " +
        COLUMN_CHAT_MESSAGE + " TEXT NOT NULL, " +
        COLUMN_CHAT_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
        "FOREIGN KEY(" + COLUMN_CHAT_GROUP_ID + ") REFERENCES " + 
        TABLE_GROUPS + "(" + COLUMN_GROUP_ID + "), " +
        "FOREIGN KEY(" + COLUMN_CHAT_USER_ID + ") REFERENCES " + 
        TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
        ")";

    // 위치 정보 테이블 생성 쿼리
    private static final String CREATE_LOCATIONS_TABLE = 
        "CREATE TABLE " + TABLE_LOCATIONS + " (" +
        COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_LOCATION_USER_ID + " INTEGER UNIQUE, " +
        COLUMN_LOCATION_LATITUDE + " REAL NOT NULL, " +
        COLUMN_LOCATION_LONGITUDE + " REAL NOT NULL, " +
        COLUMN_LOCATION_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
        "FOREIGN KEY(" + COLUMN_LOCATION_USER_ID + ") REFERENCES " + 
        TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
        ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_GROUPS_TABLE);
        db.execSQL(CREATE_MEMBERS_TABLE);
        db.execSQL(CREATE_CHATS_TABLE);
        db.execSQL(CREATE_LOCATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // 사용자 관련 메서드
    public long createUser(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, id);
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_AGREED, 0); // false
        return db.insert(TABLE_USERS, null, values);
    }

    public Cursor getUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, 
            COLUMN_USER_ID + " = ?", 
            new String[]{String.valueOf(userId)}, 
            null, null, null);
    }

    public int updateUserAgree(int userId, boolean agree) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_AGREED, agree ? 1 : 0);
        return db.update(TABLE_USERS, values, 
            COLUMN_USER_ID + " = ?", 
            new String[]{String.valueOf(userId)});
    }

    public Cursor getUserByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
            new String[]{COLUMN_USER_ID, COLUMN_USER_NAME, COLUMN_USER_AGREED},
            COLUMN_USER_NAME + " = ?",
            new String[]{name},
            null, null, null);
    }

    // 사용자 동기화 관련 메서드
    public void syncUser(int userId, String userName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_USER_NAME, userName);
        values.put(COLUMN_USER_AGREED, 1); // 동기화된 사용자는 동의한 것으로 간주

        // UPSERT 구현
        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean isUserExists(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, 
            new String[]{COLUMN_USER_ID}, 
            COLUMN_USER_ID + " = ?", 
            new String[]{String.valueOf(userId)}, 
            null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    // 그룹 관련 메서드
    public long createGroup(String name, String description, int leaderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NAME, name);
        values.put(COLUMN_GROUP_DESCRIPTION, description);
        values.put(COLUMN_GROUP_LEADER_ID, leaderId);
        long groupId = db.insert(TABLE_GROUPS, null, values);
        
        if (groupId != -1) {
            // 방장을 멤버로 추가
            ContentValues memberValues = new ContentValues();
            memberValues.put(COLUMN_MEMBER_GROUP_ID, groupId);
            memberValues.put(COLUMN_MEMBER_USER_ID, leaderId);
            db.insert(TABLE_MEMBERS, null, memberValues);
        }
        
        return groupId;
    }

    public Cursor getGroup(long groupId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_GROUPS, null, 
            COLUMN_GROUP_ID + " = ?", 
            new String[]{String.valueOf(groupId)}, 
            null, null, null);
    }

    public Cursor getAllGroups() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_GROUPS, null, null, null, null, null, 
            COLUMN_GROUP_CREATED_AT + " DESC");
    }

    // 멤버 관련 메서드
    public long addMember(long groupId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEMBER_GROUP_ID, groupId);
        values.put(COLUMN_MEMBER_USER_ID, userId);
        return db.insert(TABLE_MEMBERS, null, values);
    }

    public Cursor getGroupMembers(long groupId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MEMBERS, null, 
            COLUMN_MEMBER_GROUP_ID + " = ?", 
            new String[]{String.valueOf(groupId)}, 
            null, null, COLUMN_MEMBER_JOINED_AT + " ASC");
    }

    // 채팅 관련 메서드
    public long addChatMessage(long groupId, int userId, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_GROUP_ID, groupId);
        values.put(COLUMN_CHAT_USER_ID, userId);
        values.put(COLUMN_CHAT_MESSAGE, message);
        return db.insert(TABLE_CHATS, null, values);
    }

    public Cursor getGroupChats(long groupId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CHATS, null, 
            COLUMN_CHAT_GROUP_ID + " = ?", 
            new String[]{String.valueOf(groupId)}, 
            null, null, COLUMN_CHAT_CREATED_AT + " ASC");
    }

    // 위치 정보 관련 메서드
    public long updateUserLocation(int userId, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOCATION_USER_ID, userId);
        values.put(COLUMN_LOCATION_LATITUDE, latitude);
        values.put(COLUMN_LOCATION_LONGITUDE, longitude);

        // UPSERT 구현
        long result = db.insertWithOnConflict(TABLE_LOCATIONS, null, values, 
            SQLiteDatabase.CONFLICT_REPLACE);
        return result;
    }

    public Cursor getUserLocation(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_LOCATIONS, null, 
            COLUMN_LOCATION_USER_ID + " = ?", 
            new String[]{String.valueOf(userId)}, 
            null, null, null);
    }

    public Cursor getAllUserLocations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_LOCATIONS, null, null, null, null, null, 
            COLUMN_LOCATION_UPDATED_AT + " DESC");
    }

    // DB 상태를 JSON으로 변환
    public JSONObject getDatabaseState() throws JSONException {
        SQLiteDatabase db = this.getReadableDatabase();
        JSONObject state = new JSONObject();
        
        // 사용자 테이블
        JSONArray users = new JSONArray();
        Cursor userCursor = db.query(TABLE_USERS, null, null, null, null, null, null);
        while (userCursor.moveToNext()) {
            JSONObject user = new JSONObject();
            user.put(COLUMN_USER_ID, userCursor.getInt(userCursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.put(COLUMN_USER_NAME, userCursor.getString(userCursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
            user.put(COLUMN_USER_AGREED, userCursor.getInt(userCursor.getColumnIndexOrThrow(COLUMN_USER_AGREED)));
            users.put(user);
        }
        userCursor.close();
        state.put("users", users);

        // 그룹 테이블
        JSONArray groups = new JSONArray();
        Cursor groupCursor = db.query(TABLE_GROUPS, null, null, null, null, null, null);
        while (groupCursor.moveToNext()) {
            JSONObject group = new JSONObject();
            group.put(COLUMN_GROUP_ID, groupCursor.getInt(groupCursor.getColumnIndexOrThrow(COLUMN_GROUP_ID)));
            group.put(COLUMN_GROUP_NAME, groupCursor.getString(groupCursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)));
            group.put(COLUMN_GROUP_DESCRIPTION, groupCursor.getString(groupCursor.getColumnIndexOrThrow(COLUMN_GROUP_DESCRIPTION)));
            group.put(COLUMN_GROUP_LEADER_ID, groupCursor.getInt(groupCursor.getColumnIndexOrThrow(COLUMN_GROUP_LEADER_ID)));
            groups.put(group);
        }
        groupCursor.close();
        state.put("groups", groups);

        // 멤버 테이블
        JSONArray members = new JSONArray();
        Cursor memberCursor = db.query(TABLE_MEMBERS, null, null, null, null, null, null);
        while (memberCursor.moveToNext()) {
            JSONObject member = new JSONObject();
            member.put(COLUMN_MEMBER_GROUP_ID, memberCursor.getInt(memberCursor.getColumnIndexOrThrow(COLUMN_MEMBER_GROUP_ID)));
            member.put(COLUMN_MEMBER_USER_ID, memberCursor.getInt(memberCursor.getColumnIndexOrThrow(COLUMN_MEMBER_USER_ID)));
            member.put(COLUMN_MEMBER_JOINED_AT, memberCursor.getString(memberCursor.getColumnIndexOrThrow(COLUMN_MEMBER_JOINED_AT)));
            members.put(member);
        }
        memberCursor.close();
        state.put("members", members);

        // 채팅 테이블
        JSONArray chats = new JSONArray();
        Cursor chatCursor = db.query(TABLE_CHATS, null, null, null, null, null, null);
        while (chatCursor.moveToNext()) {
            JSONObject chat = new JSONObject();
            chat.put(COLUMN_CHAT_ID, chatCursor.getInt(chatCursor.getColumnIndexOrThrow(COLUMN_CHAT_ID)));
            chat.put(COLUMN_CHAT_GROUP_ID, chatCursor.getInt(chatCursor.getColumnIndexOrThrow(COLUMN_CHAT_GROUP_ID)));
            chat.put(COLUMN_CHAT_USER_ID, chatCursor.getInt(chatCursor.getColumnIndexOrThrow(COLUMN_CHAT_USER_ID)));
            chat.put(COLUMN_CHAT_MESSAGE, chatCursor.getString(chatCursor.getColumnIndexOrThrow(COLUMN_CHAT_MESSAGE)));
            chat.put(COLUMN_CHAT_CREATED_AT, chatCursor.getString(chatCursor.getColumnIndexOrThrow(COLUMN_CHAT_CREATED_AT)));
            chats.put(chat);
        }
        chatCursor.close();
        state.put("chats", chats);

        // 위치 테이블
        JSONArray locations = new JSONArray();
        Cursor locationCursor = db.query(TABLE_LOCATIONS, null, null, null, null, null, null);
        while (locationCursor.moveToNext()) {
            JSONObject location = new JSONObject();
            location.put(COLUMN_LOCATION_ID, locationCursor.getInt(locationCursor.getColumnIndexOrThrow(COLUMN_LOCATION_ID)));
            location.put(COLUMN_LOCATION_USER_ID, locationCursor.getInt(locationCursor.getColumnIndexOrThrow(COLUMN_LOCATION_USER_ID)));
            location.put(COLUMN_LOCATION_LATITUDE, locationCursor.getDouble(locationCursor.getColumnIndexOrThrow(COLUMN_LOCATION_LATITUDE)));
            location.put(COLUMN_LOCATION_LONGITUDE, locationCursor.getDouble(locationCursor.getColumnIndexOrThrow(COLUMN_LOCATION_LONGITUDE)));
            location.put(COLUMN_LOCATION_UPDATED_AT, locationCursor.getString(locationCursor.getColumnIndexOrThrow(COLUMN_LOCATION_UPDATED_AT)));
            locations.put(location);
        }
        locationCursor.close();
        state.put("locations", locations);

        return state;
    }

    // JSON 데이터로 DB 업데이트
    public void updateFromJSON(JSONObject data) throws JSONException {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 사용자 테이블 업데이트
            if (data.has("users")) {
                JSONArray users = data.getJSONArray("users");
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_USER_NAME, user.getString(COLUMN_USER_NAME));
                    values.put(COLUMN_USER_AGREED, user.getInt(COLUMN_USER_AGREED));
                    
                    int userId = user.getInt(COLUMN_USER_ID);
                    db.update(TABLE_USERS, values, COLUMN_USER_ID + " = ?", 
                        new String[]{String.valueOf(userId)});
                }
            }

            // 그룹 테이블 업데이트
            if (data.has("groups")) {
                JSONArray groups = data.getJSONArray("groups");
                for (int i = 0; i < groups.length(); i++) {
                    JSONObject group = groups.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_GROUP_NAME, group.getString(COLUMN_GROUP_NAME));
                    values.put(COLUMN_GROUP_DESCRIPTION, group.getString(COLUMN_GROUP_DESCRIPTION));
                    values.put(COLUMN_GROUP_LEADER_ID, group.getInt(COLUMN_GROUP_LEADER_ID));
                    
                    int groupId = group.getInt(COLUMN_GROUP_ID);
                    db.update(TABLE_GROUPS, values, COLUMN_GROUP_ID + " = ?", 
                        new String[]{String.valueOf(groupId)});
                }
            }

            // 멤버 테이블 업데이트
            if (data.has("members")) {
                JSONArray members = data.getJSONArray("members");
                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_MEMBER_GROUP_ID, member.getInt(COLUMN_MEMBER_GROUP_ID));
                    values.put(COLUMN_MEMBER_USER_ID, member.getInt(COLUMN_MEMBER_USER_ID));
                    values.put(COLUMN_MEMBER_JOINED_AT, member.getString(COLUMN_MEMBER_JOINED_AT));
                    
                    int groupId = member.getInt(COLUMN_MEMBER_GROUP_ID);
                    int userId = member.getInt(COLUMN_MEMBER_USER_ID);
                    db.update(TABLE_MEMBERS, values, 
                        COLUMN_MEMBER_GROUP_ID + " = ? AND " + COLUMN_MEMBER_USER_ID + " = ?",
                        new String[]{String.valueOf(groupId), String.valueOf(userId)});
                }
            }

            // 채팅 테이블 업데이트
            if (data.has("chats")) {
                JSONArray chats = data.getJSONArray("chats");
                for (int i = 0; i < chats.length(); i++) {
                    JSONObject chat = chats.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_CHAT_MESSAGE, chat.getString(COLUMN_CHAT_MESSAGE));
                    values.put(COLUMN_CHAT_CREATED_AT, chat.getString(COLUMN_CHAT_CREATED_AT));
                    
                    int chatId = chat.getInt(COLUMN_CHAT_ID);
                    db.update(TABLE_CHATS, values, COLUMN_CHAT_ID + " = ?", 
                        new String[]{String.valueOf(chatId)});
                }
            }

            // 위치 테이블 업데이트
            if (data.has("locations")) {
                JSONArray locations = data.getJSONArray("locations");
                for (int i = 0; i < locations.length(); i++) {
                    JSONObject location = locations.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_LOCATION_LATITUDE, location.getDouble(COLUMN_LOCATION_LATITUDE));
                    values.put(COLUMN_LOCATION_LONGITUDE, location.getDouble(COLUMN_LOCATION_LONGITUDE));
                    values.put(COLUMN_LOCATION_UPDATED_AT, location.getString(COLUMN_LOCATION_UPDATED_AT));
                    
                    int locationId = location.getInt(COLUMN_LOCATION_ID);
                    db.update(TABLE_LOCATIONS, values, COLUMN_LOCATION_ID + " = ?", 
                        new String[]{String.valueOf(locationId)});
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public long addGroup(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NAME, groupName);

        try {
            return db.insertOrThrow(TABLE_GROUPS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "그룹 추가 중 오류: " + e.getMessage());
            return -1;
        }
    }

    public boolean deleteGroup(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // 먼저 그룹 ID를 찾습니다
            Cursor cursor = db.query(TABLE_GROUPS,
                    new String[]{COLUMN_GROUP_ID},
                    COLUMN_GROUP_NAME + "=?",
                    new String[]{groupName},
                    null, null, null);

            if (cursor.moveToFirst()) {
                long groupId = cursor.getLong(0);
                cursor.close();

                // 멤버 삭제
                db.delete(TABLE_MEMBERS, COLUMN_MEMBER_GROUP_ID + "=?", new String[]{String.valueOf(groupId)});
                // 그룹 삭제
                return db.delete(TABLE_GROUPS, COLUMN_GROUP_ID + "=?", new String[]{String.valueOf(groupId)}) > 0;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "그룹 삭제 중 오류: " + e.getMessage());
            return false;
        }
    }

    public long addMember(String groupName, String memberName, String memberPhone) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // 그룹 ID 찾기
            Cursor cursor = db.query(TABLE_GROUPS,
                    new String[]{COLUMN_GROUP_ID},
                    COLUMN_GROUP_NAME + "=?",
                    new String[]{groupName},
                    null, null, null);

            if (cursor.moveToFirst()) {
                long groupId = cursor.getLong(0);
                cursor.close();

                ContentValues values = new ContentValues();
                values.put(COLUMN_MEMBER_GROUP_ID, groupId);
                values.put(COLUMN_MEMBER_NAME, memberName);
                values.put(COLUMN_MEMBER_PHONE, memberPhone);

                return db.insert(TABLE_MEMBERS, null, values);
            }
            cursor.close();
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "멤버 추가 중 오류: " + e.getMessage());
            return -1;
        }
    }

    public boolean deleteMember(String groupName, String memberName) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // 그룹 ID 찾기
            Cursor cursor = db.query(TABLE_GROUPS,
                    new String[]{COLUMN_GROUP_ID},
                    COLUMN_GROUP_NAME + "=?",
                    new String[]{groupName},
                    null, null, null);

            if (cursor.moveToFirst()) {
                long groupId = cursor.getLong(0);
                cursor.close();

                return db.delete(TABLE_MEMBERS,
                        COLUMN_MEMBER_GROUP_ID + "=? AND " + COLUMN_MEMBER_NAME + "=?",
                        new String[]{String.valueOf(groupId), memberName}) > 0;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "멤버 삭제 중 오류: " + e.getMessage());
            return false;
        }
    }
} 