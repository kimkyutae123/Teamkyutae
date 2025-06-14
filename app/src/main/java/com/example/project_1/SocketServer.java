package com.example.project_1;

import android.database.Cursor;
import android.util.Log;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;

public class SocketServer {
    private static final String TAG = "SocketServer";
    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService clientThreadPool;
    private final AtomicBoolean isRunning;
    private static final int MAX_CLIENTS = 10;
    private static final int SOCKET_TIMEOUT = 0; // 타임아웃 비활성화
    private static final int ACCEPT_TIMEOUT = 1000; // 1초
    private DatabaseHelper dbHelper;

    public SocketServer(int port) {
        this.port = port;
        this.clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        this.isRunning = new AtomicBoolean(false);
    }

    public void start() {
        if (isRunning.get()) {
            Log.w(TAG, "서버가 이미 실행 중입니다.");
            return;
        }

        new Thread(() -> {
            try {
                // 기존 서버 소켓 정리
                cleanup();
                
                // 새로운 서버 소켓 생성
                serverSocket = new ServerSocket(port);
                serverSocket.setReuseAddress(true);
                serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
                
                isRunning.set(true);
                Log.d(TAG, "서버가 시작되었습니다. 포트: " + port);

                while (isRunning.get()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        clientSocket.setSoTimeout(SOCKET_TIMEOUT);
                        clientSocket.setKeepAlive(true);
                        clientSocket.setTcpNoDelay(true);
                        
                        clientThreadPool.execute(() -> handleClient(clientSocket));
                    } catch (IOException e) {
                        if (isRunning.get()) {
                            if (e.getMessage().contains("timed out")) {
                                // 타임아웃은 정상적인 상황이므로 로그를 남기지 않음
                                continue;
                            }
                            Log.e(TAG, "클라이언트 연결 수락 중 오류: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "서버 시작 실패: " + e.getMessage());
                cleanup();
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try {
            Log.d(TAG, "새로운 클라이언트 연결: " + clientSocket.getInetAddress().getHostAddress());
            
            // 클라이언트와의 통신 처리
            while (isRunning.get() && !clientSocket.isClosed()) {
                try {
                    // 여기에 클라이언트와의 통신 로직 구현
                    Thread.sleep(100); // CPU 사용량 감소
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "클라이언트 처리 중 오류: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "클라이언트 소켓 종료 중 오류: " + e.getMessage());
            }
        }
    }

    public void stop() {
        if (!isRunning.get()) {
            return;
        }

        isRunning.set(false);
        cleanup();
        
        // 스레드 풀 종료
        clientThreadPool.shutdown();
        try {
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        Log.d(TAG, "서버가 중지되었습니다.");
    }

    private void cleanup() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "서버 소켓 종료 중 오류: " + e.getMessage());
            }
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    private void handleSync(JSONObject message, PrintWriter out) {
        try {
            // DB 동기화 로직
            JSONObject response = new JSONObject();
            response.put("type", "sync_response");
            // DB 데이터를 response에 추가
            out.println(response.toString());
            Log.d(TAG, "동기화 응답 전송");
        } catch (Exception e) {
            Log.e(TAG, "동기화 처리 중 오류: " + e.getMessage());
        }
    }

    private void handleUpdate(JSONObject message, PrintWriter out) {
        try {
            // DB 업데이트 로직
            JSONObject response = new JSONObject();
            response.put("type", "update_response");
            response.put("status", "success");
            out.println(response.toString());
            Log.d(TAG, "업데이트 응답 전송");
        } catch (Exception e) {
            Log.e(TAG, "업데이트 처리 중 오류: " + e.getMessage());
        }
    }

    private void handleUserInfo(JSONObject message, PrintWriter out) {
        try {
            // 사용자 정보 처리 로직
            JSONObject response = new JSONObject();
            response.put("type", "user_info_response");
            response.put("status", "success");
            out.println(response.toString());
            Log.d(TAG, "사용자 정보 응답 전송");
        } catch (Exception e) {
            Log.e(TAG, "사용자 정보 처리 중 오류: " + e.getMessage());
        }
    }
} 