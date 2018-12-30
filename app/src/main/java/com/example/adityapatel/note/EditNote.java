package com.example.adityapatel.note;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EditNote extends AppCompatActivity  {

    private NoteDataStore dataStore;
    private double latitude;
    private double longitude;
    private String noteId;
    private String date;
    private BroadcastReceiver broadcastReceiver;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.edit_note); Log.d("dd","in here create");
        dataStore = NoteDataStoreImpl.sharedInstance(); edName = findViewById(R.id.name);
        edNote = findViewById(R.id.note);
        mbackground = findViewById(R.id.note_background);
        int position = getIntent().getIntExtra("position",-1);
        noteData = dataStore.getNotes().get(position);
        imageBitmap = noteData.getImage();
        noteContent = noteData.getNote_content();
        noteTitle = noteData.getNote_name();
        latitude = dataStore.getNotes().get(position).getLatitude();
        longitude = dataStore.getNotes().get(position).getLongitude();
        noteId = dataStore.getNotes().get(position).getNoteId();
        imageId = dataStore.getNotes().get(position).getImageId();
        date = dataStore.getNotes().get(position).getNote_timestamp(); edName.setText(dataStore.getNotes().get(position).getNote_name()); edNote.setText(dataStore.getNotes().get(position).getNote_content()); if( imageId != null){
            imagePath = dataStore.getNotes().get(position).getImagePath();
            setBackground(dataStore.getNotes().get(position).getImagePath()); }
        mbackground.setOnClickListener(new View.OnClickListener() { @Override
        public void onClick(View v) {
            goToDisplayImage(); }
        }); }
    private void goToDisplayImage(){
        if( imageId != null){
            imageBitmap = BitmapFactory.decodeFile(imagePath); Intent i = new Intent(this, DisplayImageActivity.class);
// i.putExtra("ImagePath", imagePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream(); imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); byte[] byteArray = stream.toByteArray();
            i.putExtra("picture", byteArray);
            startActivity(i);
        } }
    @SuppressLint("NewApi")
    Page | 24
    Project Report
    private void setBackground(String path) {
        Bitmap imageBitmap = BitmapFactory.decodeFile(path);
        Drawable d = new BitmapDrawable(getResources(), imageBitmap); mbackground.setBackground(d);
        edName.setAlpha(0.75f);
        edNote.setAlpha(0.75f);
    }
    @Override
    public void onBackPressed() {
        String title = edName.getText().toString();
        String body = edNote.getText().toString();
        int position = getIntent().getIntExtra("position",-1); if(!noteTitle.equals(title) || !noteContent.equals(body) || imageFlag){
            NoteData newNote = new NoteData(currentUser.getUid(), title, body, date, latitude, longitude, noteId, imageId, imagePath);
            dataStore.updateNote(position, newNote, imageFlag); }
        finish(); }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return true; }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cemara) {
            dispatchTakePictureIntent();
            return true; }
        return super.onOptionsItemSelected(item); }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE); }
    }
    @SuppressLint({"NewApi", "ResourceAsColor"})
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            Page | 25

            Project Report
            Drawable d = new BitmapDrawable(getResources(), imageBitmap); mbackground.setBackground(d);
            edNote.setAlpha(0.75f);
            edName.setAlpha(0.75f);
            storeImage(imageBitmap); }
    }
    private void storeImage(Bitmap imageBitmap){ try {
        final File localFile = File.createTempFile(noteData.getImageId(), "jpg"); FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(localFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); } catch (Exception e) {
            e.printStackTrace(); } finally {
            try { fos.close();
            } catch (IOException e) {
                e.printStackTrace(); }
        } noteData.setImagePath(localFile.getAbsolutePath()); imagePath = localFile.getAbsolutePath();
        imageFlag = true;
    } catch (IOException e) {
        e.printStackTrace(); }
    } }