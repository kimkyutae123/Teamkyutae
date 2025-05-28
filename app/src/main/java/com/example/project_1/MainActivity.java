package com.example.project_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            String userName = sharedPreferences.getString("user_name", null);
            String userId = sharedPreferences.getString("user_id", null);

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

        // 위치 공유 버튼 처리
        Button startStopButton = findViewById(R.id.locationToggleButton);
        startStopButton.setOnClickListener(v ->
        {
            // (임시) 위치 공유 토글 기능
            Toast.makeText(MainActivity.this, "위치 공유 기능이 토글되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // 우리 학교 위치 (예시 좌표)
        LatLng school = new LatLng(37.4867918, 126.8219592);
        LatLng school2 = new LatLng(37.34420, 126.7833);
        
        // 마커 추가
        mMap.addMarker(new MarkerOptions().position(school).title("우리 학교"));
        mMap.addMarker(new MarkerOptions().position(school2).title("우리집"));
        
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(school);  // 학교 위치 추가
        builder.include(school2);  // 집 위치 추가

        LatLngBounds bounds = builder.build();

        // 두 마커를 모두 포함할 수 있는 범위로 카메라 이동
        int padding = 50;  // 화면 여백 (패딩 값)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}