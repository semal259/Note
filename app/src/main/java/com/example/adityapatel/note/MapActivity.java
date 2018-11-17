package com.example.adityapatel.note;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private NoteDataStore dataStore;
    GoogleMap mGoogleMap;
    private static final float DEFAUL_ZOOM = 10f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        supportMapFragment.getMapAsync(this);
        dataStore = NoteDataStoreImpl.sharedInstance();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            mGoogleMap = googleMap;
        }
        showMarkers();
    }

    private void showMarkers() {
        double latitude = 0;
        double longitude = 0;

        String titleNote;

        for (int i = 0; i < dataStore.getNotes().size(); i++) {
            titleNote = dataStore.getNotes().get(i).getNote_name();
            latitude = dataStore.getNotes().get(i).getLatitude();
            longitude = dataStore.getNotes().get(i).getLongitude();
            LatLng latLng = new LatLng(latitude,longitude);

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(titleNote)
                    .snippet(dataStore.getNotes().get(i).getNote_content())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_note_marker)));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
        if(dataStore.getNotes().size() == 0){
            LatLng denaliNationPark = new LatLng(63.129887, -151.197418);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(denaliNationPark, DEFAUL_ZOOM));
        }else {
            int last = dataStore.getNotes().size() - 1;
            latitude = dataStore.getNotes().get(last).getLatitude();
            longitude = dataStore.getNotes().get(last).getLongitude();
            LatLng last_note_location = new LatLng(latitude,longitude);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(last_note_location, DEFAUL_ZOOM));


        }
    }

}



