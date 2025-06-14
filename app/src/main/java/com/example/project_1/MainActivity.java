package com.example.project_1;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 메인화면 (구글 맵 위에 놓여질 버튼들 관련)
public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView searchSuggestionList;
    private SearchSuggestionAdapter suggestionAdapter;
    private List<SearchSuggestion> suggestions;
    private Button setDestinationButton;
    private Button confirmDestinationButton;
    private Button locationToggleButton;
    private Marker destinationMarker;
    private Marker currentLocationMarker;
    private Circle rangeCircle;
    private boolean isSettingDestination = false;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<Marker> testUserMarkers = new ArrayList<>();  // 클래스 변수로 추가
    private boolean isLocationSharingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 테스트용 사용자 ID 설정
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (!prefs.contains("user_id")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_id", "test_user");
            editor.putString("name_test_user", "테스트사용자");
            editor.apply();
            Log.d("TestUsers", "테스트 사용자 ID 설정됨");
        }

        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 지도 출력
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null)
        {
            mapFragment.getMapAsync(this);
        }

        initializeViews();
        setupSearchFeature();
        setupButtons();
    }

    private void initializeViews()
    {
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        searchSuggestionList = findViewById(R.id.searchSuggestionList);
        setDestinationButton = findViewById(R.id.setDestinationButton);
        confirmDestinationButton = findViewById(R.id.confirmDestinationButton);
        locationToggleButton = findViewById(R.id.locationToggleButton);
        
        // 검색 추천 목록 초기화
        suggestions = new ArrayList<>();
        suggestionAdapter = new SearchSuggestionAdapter(suggestions, suggestion ->
        {
            // 추천 항목 클릭 시 처리
            searchEditText.setText(suggestion.getName());
            searchSuggestionList.setVisibility(View.GONE);
            moveToLocation(suggestion.getLatitude(), suggestion.getLongitude(), suggestion.getName());
        });

        searchSuggestionList.setLayoutManager(new LinearLayoutManager(this));
        searchSuggestionList.setAdapter(suggestionAdapter);
    }

    private void setupSearchFeature()
    {
        // 검색어 입력 감지
        searchEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String query = s.toString().trim();
                if (!query.isEmpty())  // 한 글자부터 검색
                {
                    updateSearchSuggestions(query);
                }
                else
                {
                    searchSuggestionList.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 검색 버튼 클릭
        searchButton.setOnClickListener(v -> performSearch());

        // 엔터키 처리
        searchEditText.setOnEditorActionListener((v, actionId, event) ->
        {
            performSearch();
            return true;
        });
    }

    private void updateSearchSuggestions(String query)
    {
        // (임시) 검색어에 따른 추천 장소 생성
        suggestions.clear();
        Geocoder geocoder = new Geocoder(this);
        
        try
        {
            // 대한민국 좌표 범위 설정
            double minLat = 33.0;  // 제주도 남단
            double maxLat = 38.5;  // 강원도 북단
            double minLng = 125.0;  // 서해 서단
            double maxLng = 132.0;  // 동해 동단
            
            List<Address> addresses = geocoder.getFromLocationName(
                query + " 대한민국",  // 검색어에 대한민국 추가
                5,  // 최대 5개 결과
                minLat, minLng, maxLat, maxLng  // 대한민국 영역으로 제한
            );

            for (Address address : addresses)
            {
                String name = address.getFeatureName();
                StringBuilder addressText = new StringBuilder();
                
                // 국가명이 대한민국인 경우만 처리
                if ("KR".equals(address.getCountryCode()) || "대한민국".equals(address.getCountryName()))
                {
                    if (address.getLocality() != null)
                    {
                        addressText.append(address.getLocality());
                    }
                    if (address.getAdminArea() != null)
                    {
                        if (addressText.length() > 0)
                        {
                            addressText.append(", ");
                        }
                        addressText.append(address.getAdminArea());
                    }
                    
                    // 이름이 번지수로만 되어 있는 경우 쿼리를 이름으로 사용
                    if (name.matches("\\d+-?\\d*"))
                    {
                        name = query;
                    }
                    
                    suggestions.add(new SearchSuggestion(
                        name,
                        addressText.toString(),
                        address.getLatitude(),
                        address.getLongitude()
                    ));
                }
            }
            
            suggestionAdapter.updateSuggestions(suggestions);
            searchSuggestionList.setVisibility(suggestions.isEmpty() ? View.GONE : View.VISIBLE);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void performSearch()
    {
        String locationName = searchEditText.getText().toString().trim();
        searchSuggestionList.setVisibility(View.GONE);

        if (!locationName.isEmpty())
        {
            Geocoder geocoder = new Geocoder(MainActivity.this);
            try
            {
                // 대한민국 좌표 범위 설정
                double minLat = 33.0;  // 제주도 남단
                double maxLat = 38.5;  // 강원도 북단
                double minLng = 125.0;  // 서해 서단
                double maxLng = 132.0;  // 동해 동단
                
                List<Address> addresses = geocoder.getFromLocationName(
                    locationName + " 대한민국",  // 검색어에 대한민국 추가
                    1,  // 첫 번째 결과만
                    minLat, minLng, maxLat, maxLng  // 대한민국 영역으로 제한
                );

                if (addresses != null && !addresses.isEmpty())
                {
                    Address address = addresses.get(0);
                    
                    // 국가명이 대한민국인 경우만 처리
                    if ("KR".equals(address.getCountryCode()) || "대한민국".equals(address.getCountryName()))
                    {
                        String name = address.getFeatureName();
                        
                        // 이름이 번지수로만 되어 있는 경우 검색어를 이름으로 사용
                        if (name.matches("\\d+-?\\d*"))
                        {
                            name = locationName;
                        }
                        
                        moveToLocation(address.getLatitude(), address.getLongitude(), name);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "대한민국 내의 위치만 검색 가능합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(MainActivity.this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveToLocation(double latitude, double longitude, String title)
    {
        LatLng location = new LatLng(latitude, longitude);
        mMap.clear();  // 기존 마커 제거
        mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private void setupButtons()
    {
        // 마이페이지 버튼 처리
        Button myPageButton = findViewById(R.id.myPageButton);
        myPageButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
            startActivity(intent);
        });

        // 그룹 버튼 처리
        Button groupButton = findViewById(R.id.groupButton);
        groupButton.setOnClickListener(v ->
        {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String userName = sharedPreferences.getString("userName", null);
            String userId = sharedPreferences.getString("userId", null);

            if (userName == null || userId == null)
            {
                Toast.makeText(MainActivity.this, "마이페이지에서 이름과 아이디를 먼저 생성해주세요.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                startActivity(intent);
            }
        });

        // 목적지 설정 버튼 처리
        setDestinationButton.setOnClickListener(v ->
        {
            isSettingDestination = true;
            locationToggleButton.setVisibility(View.GONE);
            confirmDestinationButton.setVisibility(View.VISIBLE);
            
            // 현재 지도 중심에 마커 추가
            LatLng center = mMap.getCameraPosition().target;
            if (destinationMarker != null) {
                destinationMarker.remove();
            }
            destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(center)
                .title("목적지")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        });

        // 목적지 확인 버튼 처리
        confirmDestinationButton.setOnClickListener(v ->
        {
            isSettingDestination = false;
            locationToggleButton.setVisibility(View.VISIBLE);
            confirmDestinationButton.setVisibility(View.GONE);
            
            if (destinationMarker != null && currentLocationMarker != null) {
                // 범위 원 그리기
                drawRangeCircle();
                Toast.makeText(this, "목적지가 설정되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 위치 공유 버튼 처리
        locationToggleButton.setOnClickListener(v -> {
            isLocationSharingEnabled = !isLocationSharingEnabled;
            locationToggleButton.setBackgroundColor(
                    isLocationSharingEnabled ? Color.RED : Color.GRAY);

            // 동기화: 내 고유번호로 위치공유 상태 저장
            SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String myNumber = userPrefs.getString("user_number", "");
            SharedPreferences locationPrefs = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
            locationPrefs.edit().putBoolean("location_shared_" + myNumber, isLocationSharingEnabled).apply();

            if (isLocationSharingEnabled) {
                // 위치 공유가 활성화되면 테스트 사용자 마커 표시
                initializeTestUsers();
            } else {
                // 위치 공유가 비활성화되면 테스트 사용자 마커 제거
                for (Marker marker : testUserMarkers) {
                    marker.remove();
                }
                testUserMarkers.clear();
            }
        });
    }

    private void drawRangeCircle() {
        if (destinationMarker != null && currentLocationMarker != null) {
            // 기존 원 제거
            if (rangeCircle != null) {
                rangeCircle.remove();
            }

            // 두 위치 사이의 거리 계산
            LatLng currentLocation = currentLocationMarker.getPosition();
            LatLng destination = destinationMarker.getPosition();
            float[] results = new float[1];
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                destination.latitude, destination.longitude,
                results
            );
            float distanceInMeters = results[0];

            // 두 위치의 중간점 계산
            double midLat = (currentLocation.latitude + destination.latitude) / 2;
            double midLng = (currentLocation.longitude + destination.longitude) / 2;
            LatLng midPoint = new LatLng(midLat, midLng);

            // 원 그리기 (중간점을 중심으로, 거리를 반지름으로)
            rangeCircle = mMap.addCircle(new CircleOptions()
                .center(midPoint)
                .radius(distanceInMeters)
                .strokeColor(Color.argb(100, 255, 0, 0))  // 더 불투명한 빨간색 외곽선
                .fillColor(Color.argb(50, 255, 0, 0))     // 더 불투명한 빨간색 채우기
                .strokeWidth(2));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        Log.d("TestUsers", "지도 준비됨");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // 마커 클릭 시 InfoWindow 표시 설정
        mMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return true;
        });

        // 위치 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
            // 위치 공유가 활성화된 상태일 때만 테스트 사용자 초기화
            if (isLocationSharingEnabled) {
                new Handler().postDelayed(() -> {
                    Log.d("TestUsers", "지연 후 초기화 시작");
                    initializeTestUsers();
                }, 2000);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 지도 이동 리스너 설정
        mMap.setOnCameraMoveListener(() ->
        {
            if (isSettingDestination && destinationMarker != null)
            {
                destinationMarker.setPosition(mMap.getCameraPosition().target);
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            if (currentLocationMarker != null) {
                                currentLocationMarker.remove();
                            }
                            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("현재 위치"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                            // 목적지가 설정되어 있다면 범위 원 다시 그리기
                            if (destinationMarker != null) {
                                drawRangeCircle();
                            }
                        }
                    });
        }
    }

    private void initializeTestUsers() {
        // 위치 공유가 비활성화된 상태면 테스트 사용자를 표시하지 않음
        if (!isLocationSharingEnabled) {
            return;
        }

        Log.d("TestUsers", "초기화 시작");
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences groupPrefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", "");

        if (currentUserId.isEmpty()) {
            Log.d("TestUsers", "현재 사용자 ID가 없음");
            return;
        }

        if (mMap == null) {
            Log.d("TestUsers", "지도가 null");
            return;
        }

        // 현재 위치 가져오기
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("TestUsers", "위치 권한 없음");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        Log.d("TestUsers", "위치 정보 없음");
                        return;
                    }

                    Log.d("TestUsers", "위치 정보 획득: " + location.getLatitude() + ", " + location.getLongitude());

                    // 기존 마커 제거
                    for (Marker marker : testUserMarkers) {
                        marker.remove();
                    }
                    testUserMarkers.clear();

                    // 현재 위치 기준으로 테스트 사용자 위치 설정
                    double baseLat = location.getLatitude();
                    double baseLng = location.getLongitude();

                    // 테스트용 사용자 데이터
                    String[] testUserIds = {"user1", "user2", "user3"};
                    String[] testUserNames = {"김철수", "이영희", "박지민"};
                    String[] testUserNumbers = {"123456", "234567", "345678"};  // 6자리 고유번호로 변경
                    
                    // 1km 반경 내에 배치
                    double radiusInDegrees = 0.009; // 약 1km
                    double[] angles = {0, 120, 240}; // 120도 간격으로 배치

                    for (int i = 0; i < testUserIds.length; i++) {
                        String userId = testUserIds[i];
                        if (!userId.equals(currentUserId)) {
                            // 각도에 따른 위치 계산
                            double angle = Math.toRadians(angles[i]);
                            double userLat = baseLat + (radiusInDegrees * Math.cos(angle));
                            double userLng = baseLng + (radiusInDegrees * Math.sin(angle));

                            Log.d("TestUsers", "사용자 " + testUserNames[i] + " 위치: " + userLat + ", " + userLng);

                            // SharedPreferences에 데이터 저장
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("name_" + userId, testUserNames[i]);
                            editor.putString("number_" + userId, testUserNumbers[i]);  // 고유번호 저장
                            editor.putFloat("latitude_" + userId, (float) userLat);
                            editor.putFloat("longitude_" + userId, (float) userLng);
                            editor.putBoolean("is_online_" + userId, true);
                            editor.putBoolean("is_in_group_" + userId, false);  // 그룹 상태 저장
                            editor.putBoolean("has_invitation_" + userId, false);  // 초대 상태 저장
                            editor.putBoolean("has_agreed_" + userId, false);  // 동의 상태 저장
                            editor.apply();

                            // 그룹 멤버 여부 확인
                            boolean isGroupMember = groupPrefs.getBoolean("invited_" + testUserNumbers[i], false) && 
                                                  groupPrefs.getBoolean("agreed_" + testUserNumbers[i], false);

                            // 지도에 마커 추가 (그룹 멤버는 파란색, 일반 사용자는 밝은 보라색)
                            LatLng userLocation = new LatLng(userLat, userLng);
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(userLocation)
                                .title(testUserNames[i])
                                .snippet(testUserNumbers[i])  // 고유번호만 표시
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                    isGroupMember ? BitmapDescriptorFactory.HUE_AZURE : BitmapDescriptorFactory.HUE_VIOLET
                                )));
                            testUserMarkers.add(marker);
                        }
                    }

                    // 지도 이동 및 줌 레벨 조정
                    LatLng currentLocation = new LatLng(baseLat, baseLng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
                    Log.d("TestUsers", "테스트 사용자 초기화 완료");
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // SharedPreferences에서 최신 위치공유 상태를 읽어옴
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String myNumber = userPrefs.getString("user_number", "");
        SharedPreferences locationPrefs = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
        boolean isLocationShared = locationPrefs.getBoolean("location_shared_" + myNumber, false);

        isLocationSharingEnabled = isLocationShared;
        if (locationToggleButton != null) {
            locationToggleButton.setBackgroundColor(
                isLocationSharingEnabled ? Color.RED : Color.GRAY
            );
        }

        // 위치공유가 꺼져 있으면 테스트 사용자 마커 모두 제거
        if (!isLocationSharingEnabled && testUserMarkers != null) {
            for (Marker marker : testUserMarkers) {
                marker.remove();
            }
            testUserMarkers.clear();
        }
        // 위치공유가 켜져 있고 마커가 없으면 마커 다시 표시
        if (isLocationSharingEnabled && mMap != null) {
            // 그룹 상태 확인
            SharedPreferences groupPrefs = getSharedPreferences("GroupPrefs", MODE_PRIVATE);
            boolean isInGroup = groupPrefs.getBoolean("isInGroup", false);

            if (testUserMarkers.isEmpty()) {
                initializeTestUsers();
            } else if (!isInGroup) {
                // 그룹에 속해있지 않으면 모든 마커를 일반 사용자 색상으로 변경
                for (Marker marker : testUserMarkers) {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                }
            }
        }
    }
}