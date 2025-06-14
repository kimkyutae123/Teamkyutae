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
import android.view.MotionEvent;
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

import com.google.android.material.button.MaterialButton;

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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1002;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1003;
    private static final int CLIPBOARD_PERMISSION_REQUEST_CODE = 1004;
    private static final float DEFAULT_ZOOM = 15f;
    private ImageButton btnMyLocation;
    private static final int REQUEST_CHECK_SETTINGS = 1001;
    private Marker tempMarker; // 임시 마커를 위한 변수 추가

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Log.d(TAG, "앱 초기화 시작");

            // 권한 체크 및 요청
            checkAndRequestPermissions();
            Log.d(TAG, "권한 체크 완료");

            // 루트 레이아웃에 터치 리스너 추가
            View rootView = findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        hideKeyboard();
                    }
                    return false;
                });
                Log.d(TAG, "터치 리스너 설정 완료");
            } else {
                Log.w(TAG, "루트 뷰를 찾을 수 없음");
            }

            // 버튼 초기화
            try {
                initializeButtons();
                Log.d(TAG, "버튼 초기화 완료");
            } catch (Exception e) {
                Log.e(TAG, "버튼 초기화 실패: " + e.getMessage());
                e.printStackTrace();
            }

            // 위치 서비스 초기화
            try {
                initializeLocationServices();
                Log.d(TAG, "위치 서비스 초기화 완료");
            } catch (Exception e) {
                Log.e(TAG, "위치 서비스 초기화 실패: " + e.getMessage());
                e.printStackTrace();
            }

            // 지도 초기화
            try {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                    Log.d(TAG, "지도 초기화 요청 완료");
                } else {
                    Log.e(TAG, "지도 프래그먼트를 찾을 수 없음");
                }
            } catch (Exception e) {
                Log.e(TAG, "지도 초기화 실패: " + e.getMessage());
                e.printStackTrace();
            }

            // 검색 관련 초기화
            try {
                initializeSearchComponents();
                Log.d(TAG, "검색 기능 초기화 완료");
            } catch (Exception e) {
                Log.e(TAG, "검색 기능 초기화 실패: " + e.getMessage());
                e.printStackTrace();
            }

            Log.d(TAG, "앱 초기화 완료");

        } catch (Exception e) {
            Log.e(TAG, "앱 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "앱 초기화 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeButtons() {
        try {
            // 그룹 버튼 초기화
            Button groupButton = findViewById(R.id.groupButton);
            if (groupButton != null) {
                groupButton.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "그룹 버튼 클릭 처리 실패: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "그룹 페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.w(TAG, "그룹 버튼을 찾을 수 없음");
            }

            // 그룹 생성 버튼 초기화
            Button createGroupButton = findViewById(R.id.createGroupButton);
            if (createGroupButton != null) {
                createGroupButton.setOnClickListener(v -> {
                    try {
                        // 서버 IP 가져오기
                        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
                        String serverIP = prefs.getString("server_ip", "10.0.2.2");

                        Intent intent = new Intent(MainActivity.this, MakingGroupActivity.class);
                        intent.putExtra("server_ip", serverIP);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "그룹 생성 버튼 클릭 처리 실패: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "그룹 생성 페이지를 열 수 없습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Log.w(TAG, "그룹 생성 버튼을 찾을 수 없음");
            }

            // 마이페이지 버튼 초기화
            Button myPageButton = findViewById(R.id.myPageButton);
            if (myPageButton != null) {
                myPageButton.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "마이페이지 버튼 클릭 처리 실패: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "마이페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.w(TAG, "마이페이지 버튼을 찾을 수 없음");
            }

            // 목적지 버튼 초기화
            MaterialButton setDestinationButton = findViewById(R.id.setDestinationButton);
            MaterialButton locationToggleButton = findViewById(R.id.locationToggleButton);
            MaterialButton confirmDestinationButton = findViewById(R.id.confirmDestinationButton);

            if (setDestinationButton != null) {
                setDestinationButton.setOnClickListener(v -> {
                    if (map != null) {
                        // 위치 공유 버튼 숨기기
                        locationToggleButton.setVisibility(View.GONE);
                        
                        // 임시 마커 추가
                        LatLng center = map.getCameraPosition().target;
                        if (tempMarker != null) {
                            tempMarker.remove();
                        }
                        tempMarker = map.addMarker(new MarkerOptions()
                                .position(center)
                                .title("임시 목적지")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                        
                        // 목적지 설정 버튼 표시
                        confirmDestinationButton.setVisibility(View.VISIBLE);
                        
                        // 지도 이동 리스너 추가
                        map.setOnCameraMoveListener(() -> {
                            if (tempMarker != null) {
                                tempMarker.setPosition(map.getCameraPosition().target);
                            }
                        });
                    }
                });
            }

            // 목적지 설정 버튼 초기화
            if (confirmDestinationButton != null) {
                confirmDestinationButton.setOnClickListener(v -> {
                    if (map != null && tempMarker != null) {
                        // 임시 마커를 실제 목적지 마커로 변경
                        LatLng position = tempMarker.getPosition();
                        if (destinationMarker != null) {
                            destinationMarker.remove();
                        }
                        destinationMarker = map.addMarker(new MarkerOptions()
                                .position(position)
                                .title("목적지")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        
                        // 임시 마커 제거
                        tempMarker.remove();
                        tempMarker = null;
                        
                        // 목적지 설정 버튼 숨기기
                        confirmDestinationButton.setVisibility(View.GONE);
                        
                        // 위치 공유 버튼 다시 표시
                        locationToggleButton.setVisibility(View.VISIBLE);
                        
                        // 지도 이동 리스너 제거
                        map.setOnCameraMoveListener(null);
                        
                        Toast.makeText(this, "목적지가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "버튼 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e; // 상위 메서드에서 처리하도록 예외 전파
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
    }

    private void initializeSearchComponents() {
        initializeViews();
        setupSearchFeature();
    }

    private void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
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
}