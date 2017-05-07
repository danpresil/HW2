package com.example.dan_p.hw2.activities;


import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

import com.example.dan_p.hw2.R;
import com.example.dan_p.hw2.database.PlayerRecord;
import com.example.dan_p.hw2.database.RecordsOpenHelper;
import com.example.dan_p.hw2.minesweeper.DifficultyLevel;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity implements OnMapReadyCallback{
    private RadioGroup radioGroup;

    private GoogleMap googleMap;
    private ArrayList<PlayerRecord> playerRecordArrayList;

    private RecordsOpenHelper db;
    private String levelName = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Bundle b = getIntent().getExtras();

        if (b != null)
            levelName = b.getString("level");
        else
            levelName = DifficultyLevel.EASY.toString();

        db = new RecordsOpenHelper(getApplicationContext());
        playerRecordArrayList = db.getTopRecords(levelName, 10);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.table_placeholder_leaderboard, new TableFragment().newInstance(playerRecordArrayList, levelName));
        ft.commit();

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        boolean hasMarkers = false;
        if (playerRecordArrayList.size() >0 ) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (int i = 0 ; i < playerRecordArrayList.size() ; i++) {
                PlayerRecord record = playerRecordArrayList.get(i);
                if (record.getLatitude() != 0) {
                    hasMarkers = true;
                    Marker marker = googleMap.addMarker(new MarkerOptions().flat(true)
                            .position(new LatLng(record.getLatitude(), record.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                            .rotation((float)record.getRotation())
                            .title(record.getName() + " - " + record.getTime()));
                    builder.include(marker.getPosition());
                }
            }

            if (hasMarkers) {
                LatLngBounds bounds = builder.build();
                int padding = 500;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                this.googleMap.moveCamera(cu);

            }
        }
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
    }
}

