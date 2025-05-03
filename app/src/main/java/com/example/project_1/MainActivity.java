package com.example.project_1;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);  // <-- .xml 제거
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // 버튼 클릭 시 실행할 코드
            Toast.makeText(this, "설정 버튼 클릭됨", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
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
}
