package com.example.adityapatel.note;

import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NewNote extends AppCompatActivity {

    public static final String TAG = "ss";
    EditText edName, edNote;
    private NoteDataStore dataStore;
    private static final int LOCTION_PERMISSION_REQUEST_CODE = 123;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = false;
    private static LatLng currentlatlng;

  //  private NoteLocation noteLocation = null;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private boolean mbound = false;
    private Location mlocation;
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NoteLocation.LocalBinder localBinder = (NoteLocation.LocalBinder) service;
            mbound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_new__note);
        super.onCreate(savedInstanceState);
        edName = findViewById(R.id.name);
        edNote = findViewById(R.id.note);
        dataStore = NoteDataStoreImpl.sharedInstance(getApplicationContext());
        getLocationPermission();
        //getDeviceLocation();

    }


    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location ) task.getResult();
                            currentlatlng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

                        }else {
                            Log.d(TAG, "onComplete: Not found location");
                            currentlatlng = new LatLng(63.129887, -151.197418);
                            Toast.makeText(NewNote.this,"unable to get current location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.d(TAG, "getDeviceLocation: Exception "+e.getMessage());
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onback");
        Toast.makeText(NewNote.this, "Back Press", Toast.LENGTH_SHORT).show();
        String name = edName.getText().toString();
        String note_content = edNote.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(note_content)) {
            finish();
            return;
        }
        NoteData newNote = new NoteData(currentFirebaseUser.getUid(),name, note_content, getTimeStamp(), currentlatlng.latitude, currentlatlng.longitude);
        dataStore.addNote(newNote);
        finish();
        /*Intent intent;
        intent = new Intent(NewNote.this, MainActivity.class);
        startActivity(intent);*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public static String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        String currentDateTime = dateFormat.format(new Date());
        return currentDateTime;
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                getDeviceLocation();

            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCTION_PERMISSION_REQUEST_CODE);

            }

        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCTION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCTION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                    //initialize map
                }
            }
        }

    }


}
