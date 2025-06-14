package com.example.project_1;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONArray;

public class SocketClient {
    private static final String TAG = "SocketClient";
    private final String serverIp;
    private final int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final AtomicBoolean isConnected;
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int CONNECTION_TIMEOUT = 5000;
    private Context context;
    private DatabaseHelper dbHelper;
    private OnMessageListener messageListener;

    public SocketClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.isConnected = new AtomicBoolean(false);
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public void connect() {
        if (isConnected.get()) {
            Log.d(TAG, "이미 연결되어 있습니다.");
            return;
        }

        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverIp, serverPort), CONNECTION_TIMEOUT);
                isConnected.set(true);
                Log.d(TAG, "서버에 연결되었습니다.");

                // 동기화 요청
                try {
                    JSONObject syncRequest = new JSONObject();
                    syncRequest.put("type", "sync");
                    sendMessage(syncRequest.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "동기화 요청 생성 중 오류: " + e.getMessage());
                }

                // 메시지 수신 시작
                startMessageReceiver();
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "서버 연결 시간 초과: " + e.getMessage());
                disconnect();
            } catch (ConnectException e) {
                Log.e(TAG, "서버 연결 실패: " + e.getMessage());
                disconnect();
            } catch (IOException e) {
                Log.e(TAG, "연결 중 오류 발생: " + e.getMessage());
                disconnect();
            }
        }).start();
    }

    private void startMessageReceiver() {
        new Thread(() -> {
            try {
                String message;
                while (isConnected.get() && (message = in.readLine()) != null) {
                    try {
                        handleMessage(message);
                    } catch (JSONException e) {
                        Log.e(TAG, "메시지 처리 중 JSON 오류: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "메시지 수신 중 오류: " + e.getMessage());
                isConnected.set(false);
                cleanup();
            }
        }).start();
    }

    private void handleMessage(String message) throws JSONException {
        JSONObject json = new JSONObject(message);
        String type = json.getString("type");
        
        switch (type) {
            case "sync":
                handleSyncMessage(json);
                break;
            case "group":
                handleGroupMessage(json);
                break;
            case "member":
                handleMemberMessage(json);
                break;
            default:
                Log.w(TAG, "알 수 없는 메시지 타입: " + type);
        }
    }

    private void handleSyncMessage(JSONObject json) throws JSONException {
        JSONArray groups = json.getJSONArray("groups");
        JSONArray members = json.getJSONArray("members");

        // 그룹 동기화
        for (int i = 0; i < groups.length(); i++) {
            JSONObject group = groups.getJSONObject(i);
            String groupName = group.getString("name");
            dbHelper.addGroup(groupName);
        }

        // 멤버 동기화
        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.getJSONObject(i);
            String groupName = member.getString("group_name");
            String memberName = member.getString("name");
            String memberPhone = member.getString("phone");
            dbHelper.addMember(groupName, memberName, memberPhone);
        }
    }

    private void handleGroupMessage(JSONObject json) throws JSONException {
        String action = json.getString("action");
        String groupName = json.getString("group_name");

        switch (action) {
            case "create":
                dbHelper.addGroup(groupName);
                break;
            case "delete":
                dbHelper.deleteGroup(groupName);
                break;
        }
    }

    private void handleMemberMessage(JSONObject json) throws JSONException {
        String action = json.getString("action");
        String groupName = json.getString("group_name");
        String memberName = json.getString("member_name");
        String memberPhone = json.getString("member_phone");

        switch (action) {
            case "add":
                dbHelper.addMember(groupName, memberName, memberPhone);
                break;
            case "remove":
                dbHelper.deleteMember(groupName, memberName);
                break;
        }
    }

    private void handleSync(JSONObject data) {
        try {
            dbHelper.updateFromJSON(data);
            Log.d(TAG, "DB 동기화 완료");
        } catch (Exception e) {
            Log.e(TAG, "DB 동기화 중 오류: " + e.getMessage());
        }
    }

    private void handleUpdate(JSONObject data) {
        try {
            String table = data.getString("table");
            JSONObject updateData = new JSONObject();
            updateData.put(table, data.getJSONObject("data"));
            dbHelper.updateFromJSON(updateData);
            Log.d(TAG, "DB 업데이트 완료: " + table);
        } catch (Exception e) {
            Log.e(TAG, "DB 업데이트 중 오류: " + e.getMessage());
        }
    }

    private void handleUserInfo(JSONObject data) {
        try {
            int userId = data.getInt("userId");
            String userName = data.getString("userName");
            
            // 사용자 정보를 DB에 저장
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("user_name", userName);
            db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            
            Log.d(TAG, "사용자 정보 업데이트 완료: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "사용자 정보 처리 중 오류: " + e.getMessage());
        }
    }

    public boolean sendMessage(String message) {
        if (!isConnected.get()) {
            Log.e(TAG, "서버에 연결되어 있지 않습니다.");
            return false;
        }

        try {
            // 메시지 크기 제한 (1MB)
            if (message.length() > 1024 * 1024) {
                Log.e(TAG, "메시지 크기가 너무 큽니다: " + message.length() + " bytes");
                return false;
            }
            
            out.println(message);
            out.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "메시지 전송 중 오류: " + e.getMessage());
            isConnected.set(false);
            return false;
        }
    }

    public String receiveMessage() {
        if (!isConnected.get()) {
            Log.e(TAG, "서버에 연결되어 있지 않습니다.");
            return null;
        }

        try {
            return in.readLine();
        } catch (IOException e) {
            Log.e(TAG, "메시지 수신 중 오류: " + e.getMessage());
            isConnected.set(false);
            return null;
        }
    }

    public boolean isConnected() {
        return isConnected.get() && socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void cleanup() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            Log.e(TAG, "리소스 정리 중 오류: " + e.getMessage());
        }
    }

    public interface OnMessageListener {
        void onMessage(JSONObject message);
    }

    public void setMessageListener(OnMessageListener listener) {
        this.messageListener = listener;
    }

    public void disconnect() {
        cleanup();
        isConnected.set(false);
        Log.d(TAG, "서버와의 연결이 종료되었습니다.");
    }
} 