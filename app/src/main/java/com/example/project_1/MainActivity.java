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

// 메인화면 (구글 맵 위에 놓여질 버튼들 관련)
public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "초기화 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                        // 목적지 설정 버튼 표시
                        if (confirmDestinationButton != null) {
                            confirmDestinationButton.setVisibility(View.VISIBLE);
                        }
                        // 위치 공유 버튼 숨기기
                        if (locationToggleButton != null) {
                            locationToggleButton.setVisibility(View.GONE);
                        }
                        // 지도 중심에 마커 생성
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
                        // 목적지 설정 버튼 숨기기
                        if (confirmDestinationButton != null) {
                            confirmDestinationButton.setVisibility(View.GONE);
                        }
                        // 위치 공유 버튼 표시
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
                        // 목적지 설정 완료
                        isSettingDestination = false;
                        confirmDestinationButton.setVisibility(View.GONE);
                        // 위치 공유 버튼 표시
                        if (locationToggleButton != null) {
                            locationToggleButton.setVisibility(View.VISIBLE);
                        }
                        // 여기에 목적지 설정 완료 후 처리 로직 추가
                    }
                });
            }

            // 위치 공유 버튼
            locationToggleButton = findViewById(R.id.locationToggleButton);
            if (locationToggleButton != null) {
                locationToggleButton.setOnClickListener(v -> {
                    // (임시) 위치 공유 토글 기능
                    Toast.makeText(MainActivity.this, "위치 공유 기능이 토글되었습니다.", Toast.LENGTH_SHORT).show();
                });
            }

            // 마이페이지 버튼
            Button myPageButton = findViewById(R.id.myPageButton);
            if (myPageButton != null) {
                myPageButton.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                    startActivity(intent);
                });
            }

            // 그룹 버튼
            Button groupButton = findViewById(R.id.groupButton);
            if (groupButton != null) {
                groupButton.setOnClickListener(v -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String userName = sharedPreferences.getString("user_name", null);
                    String userId = sharedPreferences.getString("user_id", null);

                    if (userName == null || userId == null) {
                        Toast.makeText(MainActivity.this, "마이페이지에서 이름과 아이디를 먼저 생성해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                        startActivity(intent);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        map.clear();  // 기존 마커 제거
        map.addMarker(new MarkerOptions().position(location).title(title));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private void createLocationRequest() {
        try {
            locationRequest = new LocationRequest.Builder(10000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateIntervalMillis(5000)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "위치 요청 생성 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                try {
                    for (Location location : locationResult.getLocations()) {
                        updateCurrentLocation(location);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void updateCurrentLocation(Location location) {
        try {
            if (map == null || location == null) return;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            
            if (currentLocationMarker == null) {
                // 첫 위치 업데이트 시 마커 생성
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title("현재 위치")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                currentLocationMarker = map.addMarker(markerOptions);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                // 기존 마커 위치 업데이트
                currentLocationMarker.setPosition(latLng);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        try {
            map = googleMap;
            
            // 지도 설정
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(true);
            
            // 카메라 이동 리스너 추가
            map.setOnCameraMoveListener(() -> {
                if (isSettingDestination && destinationMarker != null) {
                    // 지도가 움직일 때 마커도 함께 이동
                    destinationMarker.setPosition(map.getCameraPosition().target);
                }
            });

            // 위치 권한 확인 및 업데이트 시작
            if (checkLocationPermission()) {
                startLocationUpdates();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "지도 초기화 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermission() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startLocationUpdates() {
        try {
            if (checkLocationPermission() && map != null) {
                map.setMyLocationEnabled(true);
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "위치 업데이트 시작 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            startLocationUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (fusedLocationClient != null && locationCallback != null) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}