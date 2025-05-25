package com.example.project_1;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Message> messageList;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 지도 출력
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 검색 처리
        EditText searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String locationName = searchEditText.getText().toString();
            if (!locationName.isEmpty()) {
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    } else {
                        Toast.makeText(MainActivity.this, "위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(MainActivity.this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 마이페이지 버튼
        Button myPageButton = findViewById(R.id.myPageButton);
        myPageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
            startActivity(intent);
        });

        // 그룹 버튼
        Button groupButton = findViewById(R.id.groupButton);
        groupButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GroupActivity.class);
            startActivity(intent);
        });

        // 위치 공유 버튼
        Button startStopButton = findViewById(R.id.locationToggleButton);
        startStopButton.setOnClickListener(v -> {
            // 향후 기능 추가 예정
        });

        // 새로고침 버튼
        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> {
            loadMessages();
        });

        // 메시지 목록 설정
        RecyclerView messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        messageRecyclerView.setAdapter(adapter);

        // 앱 시작 시 메시지 불러오기
        loadMessages();
    }

    private void loadMessages() {
        RetrofitClient.getInstance().getMessages().enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messageList.clear();
                    messageList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "메시지 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng school = new LatLng(37.4867918, 126.8219592);
        LatLng school2 = new LatLng(37.34420, 126.7833);
        mMap.addMarker(new MarkerOptions().position(school).title("우리 학교"));
        mMap.addMarker(new MarkerOptions().position(school2).title("우리집"));

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(school)
                .include(school2)
                .build();

        int padding = 50;
        mMap.setOnMapLoadedCallback(() -> {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}