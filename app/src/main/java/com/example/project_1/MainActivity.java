package com.example.project_1;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.Inet4Address;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import java.net.InetSocketAddress;
import java.net.Socket;

import android.media.AudioManager;

import android.os.Build;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;
import android.content.IntentSender;
import android.app.PendingIntent;

import java.net.ServerSocket;

// 메인화면 (구글 맵 위에 놓여질 버튼들 관련)
public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{
    private static final String TAG = "MainActivity";
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker currentLocationMarker;
    private Marker destinationMarker;
    private boolean isSettingDestination = false;
    private Button setDestinationButton;
    private Button locationToggleButton;
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView searchSuggestionList;
    private SearchSuggestionAdapter suggestionAdapter;
    private List<SearchSuggestion> suggestions;
    private Button confirmDestinationButton;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1002;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1003;
    private static final int CLIPBOARD_PERMISSION_REQUEST_CODE = 1004;
    private static final float DEFAULT_ZOOM = 15f;
    private ImageButton btnMyLocation;
    private SocketClient socketClient;
    private SocketServer socketServer;
    private boolean isServer = false;
    private DatabaseHelper dbHelper;
    private static final int SERVER_PORT = 8080;
    private String serverIp = null;
    private static final int REQUEST_CHECK_SETTINGS = 1001;
    private ServerSocket serverSocket;
    private boolean isServerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // 오디오 시스템 초기화
            initializeAudioSystem();

            // 권한 체크 및 요청
            checkAndRequestPermissions();

            // 서버 상태 확인
            checkServerStatus();

            // 루트 레이아웃에 터치 리스너 추가
            View rootLayout = findViewById(R.id.rootLayout);
            if (rootLayout != null) {
                rootLayout.setOnClickListener(v -> {
                    View currentFocus = getCurrentFocus();
                    if (currentFocus != null) {
                        currentFocus.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                        }
                    }
                });
            }

            // 위치 서비스 초기화
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            createLocationRequest();
            createLocationCallback();

            // 지도 초기화
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            // 버튼 초기화
            initializeButtons();

            // 기존 기능 초기화
            initializeViews();
            setupSearchFeature();

            // 소켓 통신 초기화
            initializeSocketCommunication();

            // DatabaseHelper 초기화
            dbHelper = new DatabaseHelper(this);
        } catch (Exception e) {
            Log.e(TAG, "onCreate error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "앱 초기화 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeButtons() {
        try {
            // 목적지 버튼
            setDestinationButton = findViewById(R.id.setDestinationButton);
            confirmDestinationButton = findViewById(R.id.confirmDestinationButton);
            
            if (setDestinationButton != null) {
                setDestinationButton.setOnClickListener(v -> {
                    if (!isSettingDestination) {
                        isSettingDestination = true;
                        if (confirmDestinationButton != null) {
                            confirmDestinationButton.setVisibility(View.VISIBLE);
                        }
                        if (locationToggleButton != null) {
                            locationToggleButton.setVisibility(View.GONE);
                        }
                        if (map != null) {
                            LatLng center = map.getCameraPosition().target;
                            if (destinationMarker != null) {
                                destinationMarker.remove();
                            }
                            destinationMarker = map.addMarker(new MarkerOptions()
                                    .position(center)
                                    .title("목적지")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                    } else {
                        isSettingDestination = false;
                        if (confirmDestinationButton != null) {
                            confirmDestinationButton.setVisibility(View.GONE);
                        }
                        if (locationToggleButton != null) {
                            locationToggleButton.setVisibility(View.VISIBLE);
                        }
                        if (destinationMarker != null) {
                            destinationMarker.remove();
                            destinationMarker = null;
                        }
                    }
                });
            }

            // 목적지 설정 버튼
            if (confirmDestinationButton != null) {
                confirmDestinationButton.setOnClickListener(v -> {
                    if (destinationMarker != null) {
                        isSettingDestination = false;
                        confirmDestinationButton.setVisibility(View.GONE);
                        if (locationToggleButton != null) {
                            locationToggleButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            // 위치 공유 버튼
            locationToggleButton = findViewById(R.id.locationToggleButton);
            if (locationToggleButton != null) {
                locationToggleButton.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "위치 공유 기능이 토글되었습니다.", Toast.LENGTH_SHORT).show();
                });
            }

            // 마이페이지 버튼
            Button myPageButton = findViewById(R.id.myPageButton);
            if (myPageButton != null) {
                myPageButton.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "마이페이지 이동 오류: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "마이페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // 그룹 버튼
            Button groupButton = findViewById(R.id.groupButton);
            if (groupButton != null) {
                groupButton.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "그룹 페이지 이동 오류: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "그룹 페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // 그룹 생성 버튼 초기화
            Button createGroupButton = findViewById(R.id.createGroupButton);
            if (createGroupButton != null) {
                createGroupButton.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, MakingGroupActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "그룹 생성 페이지 이동 오류: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "그룹 생성 페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "버튼 초기화 오류: " + e.getMessage());
            Toast.makeText(this, "버튼 초기화 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews()
    {
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        searchSuggestionList = findViewById(R.id.searchSuggestionList);
        
        // 검색 추천 목록 초기화
        suggestions = new ArrayList<>();
        suggestionAdapter = new SearchSuggestionAdapter(suggestions, suggestion ->
        {
            if (searchEditText != null) {
                searchEditText.setText(suggestion.getName());
            }
            if (searchSuggestionList != null) {
                searchSuggestionList.setVisibility(View.GONE);
            }
            moveToLocation(suggestion.getLatitude(), suggestion.getLongitude(), suggestion.getName());
        });

        searchSuggestionList.setLayoutManager(new LinearLayoutManager(this));
        searchSuggestionList.setAdapter(suggestionAdapter);
    }

    private void setupSearchFeature() {
        try {
            if (searchEditText != null) {
                searchEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String query = s.toString().trim();
                        if (!query.isEmpty()) {
                            updateSearchSuggestions(query);
                        } else {
                            if (searchSuggestionList != null) {
                                searchSuggestionList.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                // 검색 버튼 클릭 리스너
                if (searchButton != null) {
                    searchButton.setOnClickListener(v -> {
                        String query = searchEditText.getText().toString().trim();
                        if (!query.isEmpty()) {
                            performSearch(query);
                        }
                    });
                }

                // 엔터키 처리
                searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                        return true;
                    }
                    return false;
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "검색 기능 설정 오류: " + e.getMessage());
        }
    }

    private void performSearch(String query) {
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = geocoder.getFromLocationName(query, 5);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                String locationName = address.getAddressLine(0);
                
                moveToLocation(latitude, longitude, locationName);
                if (searchSuggestionList != null) {
                    searchSuggestionList.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "검색 수행 오류: " + e.getMessage());
            Toast.makeText(this, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveToLocation(double latitude, double longitude, String title) {
        try {
            if (map != null) {
                LatLng location = new LatLng(latitude, longitude);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
                
                // 마커 추가
                if (destinationMarker != null) {
                    destinationMarker.remove();
                }
                destinationMarker = map.addMarker(new MarkerOptions()
                        .position(location)
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        } catch (Exception e) {
            Log.e(TAG, "위치 이동 오류: " + e.getMessage());
        }
    }

    private void updateSearchSuggestions(String query) {
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = geocoder.getFromLocationName(query, 5);
            
            if (addresses != null && !addresses.isEmpty()) {
                suggestions.clear();
                for (Address address : addresses) {
                    String name = address.getFeatureName();
                    String fullAddress = address.getAddressLine(0);
                    double latitude = address.getLatitude();
                    double longitude = address.getLongitude();
                    suggestions.add(new SearchSuggestion(name, fullAddress, latitude, longitude));
                }
                
                if (suggestionAdapter != null) {
                    suggestionAdapter.notifyDataSetChanged();
                }
                
                if (searchSuggestionList != null) {
                    searchSuggestionList.setVisibility(View.VISIBLE);
                }
            } else {
                if (searchSuggestionList != null) {
                    searchSuggestionList.setVisibility(View.GONE);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "검색 추천 업데이트 오류: " + e.getMessage());
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateCurrentLocation(location);
                }
            }
        };
    }

    private void updateCurrentLocation(Location location) {
        try {
            if (map != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (currentLocationMarker != null) {
                    currentLocationMarker.remove();
                }
                currentLocationMarker = map.addMarker(new MarkerOptions()
                        .position(currentLatLng)
                        .title("현재 위치"));
            }
        } catch (Exception e) {
            Log.e(TAG, "위치 업데이트 오류: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        try {
            map = googleMap;
            if (checkLocationPermission()) {
                map.setMyLocationEnabled(true);
                startLocationUpdates();
            } else {
                requestLocationPermission();
            }

            // 기본 위치 설정 (서울)
            LatLng seoul = new LatLng(37.5665, 126.9780);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, DEFAULT_ZOOM));
        } catch (Exception e) {
            Log.e(TAG, "지도 초기화 오류: " + e.getMessage());
            Toast.makeText(this, "지도 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 기본 위치 권한이 승인되면 백그라운드 위치 권한 요청
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    }
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                }
                break;

            case BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "백그라운드 위치 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                }
                break;

            case STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "저장소 권한이 승인되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "저장소 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                }
                break;

            case CLIPBOARD_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "클립보드 권한이 승인되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "클립보드 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void startLocationUpdates() {
        try {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000)
                    .setFastestInterval(5000);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this)
                    .checkLocationSettings(builder.build());

            task.addOnSuccessListener(this, locationSettingsResponse -> {
                // 위치 설정이 활성화되어 있음
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
            });

            task.addOnFailureListener(this, e -> {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.e(TAG, "위치 설정 확인 중 오류 발생", sendEx);
                        Toast.makeText(this, "위치 설정을 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "위치 설정 확인 중 오류 발생", e);
                    Toast.makeText(this, "위치 설정을 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "위치 업데이트 시작 실패: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
            .setTitle("앱 종료")
            .setMessage("앱을 종료하시겠습니까?")
            .setPositiveButton("예", (dialog, which) -> {
                finish();
            })
            .setNegativeButton("아니오", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (socketClient != null && socketClient.isConnected()) {
            socketClient.disconnect();
        }
        if (socketServer != null) {
            socketServer.stop();
        }
        if (isServerRunning) {
            stopServer();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void initializeSocketCommunication() {
        try {
            if (isServer) {
                Log.d(TAG, "서버 모드로 시작");
                startServerMode();
            } else {
                Log.d(TAG, "클라이언트 모드로 시작");
                initializeSocketClient();
            }
        } catch (Exception e) {
            Log.e(TAG, "소켓 초기화 중 오류 발생: " + e.getMessage());
            Toast.makeText(this, "초기화 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSync(JSONObject message) throws JSONException {
        // DB 동기화 처리
        JSONObject data = message.getJSONObject("data");
        dbHelper.updateFromJSON(data);
        
        // UI 업데이트
        runOnUiThread(() -> {
            // TODO: 필요한 UI 업데이트 로직 구현
            Toast.makeText(this, "데이터가 동기화되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleUpdate(JSONObject message) throws JSONException {
        // DB 업데이트 처리
        String table = message.getString("table");
        JSONObject data = message.getJSONObject("data");
        
        JSONObject updateData = new JSONObject();
        updateData.put(table, data);
        dbHelper.updateFromJSON(updateData);
        
        // UI 업데이트
        runOnUiThread(() -> {
            // TODO: 필요한 UI 업데이트 로직 구현
            Toast.makeText(this, "데이터가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkAndRequestPermissions() {
        // 위치 권한 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 저장소 권한 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                STORAGE_PERMISSION_REQUEST_CODE);
        }

        // Android 10 이상에서 백그라운드 위치 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void checkServerStatus() {
        new Thread(() -> {
            try {
                // 에뮬레이터 IP 주소 가져오기
                String localIp = getLocalIpAddress();
                Log.d(TAG, "현재 기기 IP: " + localIp);

                // 서버 IP가 설정되어 있지 않으면 현재 IP를 서버 IP로 설정
                if (serverIp == null) {
                    serverIp = localIp;
                }

                // 서버 연결 시도
                Socket testSocket = new Socket();
                testSocket.connect(new InetSocketAddress(serverIp, SERVER_PORT), 1000);
                testSocket.close();
                
                // 서버가 이미 실행 중
                isServer = false;
                runOnUiThread(() -> {
                    Log.d(TAG, "클라이언트 모드로 실행됩니다. 서버 IP: " + serverIp);
                    initializeSocketCommunication();
                });
            } catch (IOException e) {
                // 서버가 없음
                isServer = true;
                runOnUiThread(() -> {
                    Log.d(TAG, "이 앱이 서버로 설정되었습니다. IP: " + serverIp);
                    initializeSocketCommunication();
                });
            }
        }).start();
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "IP 주소 가져오기 실패: " + e.getMessage());
        }
        return "127.0.0.1";
    }

    private boolean isPortInUse(int port) {
        try {
            ServerSocket testSocket = new ServerSocket(port);
            testSocket.close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private void startServerMode() {
        try {
            // 서버 시작
            serverSocket = new ServerSocket(SERVER_PORT);
            isServerRunning = true;
            
            // PC의 IP 주소 가져오기
            String serverIP = getLocalIpAddress();
            
            // 서버 IP 저장
            SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("server_ip", serverIP);
            editor.apply();

            // 서버 시작 메시지
            Toast.makeText(this, "서버가 시작되었습니다. IP: " + serverIP, Toast.LENGTH_LONG).show();
            
            // 클라이언트 연결 수락 스레드 시작
            new Thread(this::acceptClients).start();
        } catch (IOException e) {
            Log.e(TAG, "서버 시작 실패: " + e.getMessage());
            Toast.makeText(this, "서버 시작에 실패했습니다.", Toast.LENGTH_SHORT).show();
            isServerRunning = false;
        }
    }

    private void acceptClients() {
        while (isServerRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClientConnection(clientSocket)).start();
            } catch (IOException e) {
                Log.e(TAG, "클라이언트 연결 수락 중 오류 발생: " + e.getMessage());
            }
        }
    }

    private void handleClientConnection(Socket clientSocket) {
        try {
            // 클라이언트와의 통신 처리
            // 이 부분은 실제 구현에 따라 달라질 수 있습니다.
            // 예를 들어, 스트림을 통해 데이터를 읽고 쓰는 등의 작업을 수행할 수 있습니다.
            // 여기서는 간단하게 연결을 유지하는 것으로 가정합니다.
            clientSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "클라이언트와의 통신 중 오류 발생: " + e.getMessage());
        }
    }

    // 오디오 시스템 초기화
    private void initializeAudioSystem() {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setSpeakerphoneOn(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "오디오 시스템 초기화 중 오류: " + e.getMessage());
        }
    }

    private void initializeSocketClient() {
        // 서버 IP 가져오기
        String serverIP = getServerIP();
        if (serverIP == null || serverIP.isEmpty()) {
            Toast.makeText(this, "서버 IP가 설정되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 소켓 클라이언트 초기화
        socketClient = new SocketClient(serverIP, SERVER_PORT);
        socketClient.connect();
    }

    private String getServerIP() {
        SharedPreferences prefs = getSharedPreferences("ServerPrefs", MODE_PRIVATE);
        return prefs.getString("server_ip", null);
    }

    private void showMakingGroupDialog() {
        // 서버 IP 가져오기
        String serverIP = getServerIP();
        if (serverIP == null || serverIP.isEmpty()) {
            Toast.makeText(this, "서버 IP가 설정되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 그룹 생성 화면으로 이동
        Intent intent = new Intent(this, MakingGroupActivity.class);
        intent.putExtra("server_ip", serverIP);
        startActivity(intent);
    }

    private void stopServer() {
        if (isServerRunning) {
            try {
                serverSocket.close();
                isServerRunning = false;
                Toast.makeText(this, "서버가 종료되었습니다.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "서버 종료 중 오류 발생: " + e.getMessage());
            }
        }
    }
}