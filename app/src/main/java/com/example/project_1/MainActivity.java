package com.example.project_1;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

// 메인화면 (구글 맵 위에 놓여질 버튼들 관련)
public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{
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

        // 검색 처리
        EditText searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v ->
        {
            String locationName = searchEditText.getText().toString();

            if (!locationName.isEmpty())
            {
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try
                {
                    List<Address> addresses = geocoder.getFromLocationName(locationName, 1);

                    if (addresses != null && !addresses.isEmpty())
                    {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(MainActivity.this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

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
            Intent intent = new Intent(MainActivity.this, GroupActivity.class);
            startActivity(intent);
        });

        // 위치 공유 버튼 처리
        Button startStopButton = findViewById(R.id.locationToggleButton);
        startStopButton.setOnClickListener(v ->
        {
            // 코드 향후 추가
        });
    }

    private GoogleMap mMap;

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
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));  // 한번만 이동

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);  // <-- .xml 제거
        return true;
    }
}