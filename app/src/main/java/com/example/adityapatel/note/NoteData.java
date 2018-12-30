package com.example.adityapatel.note;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteData {
    private String userId;
    private String noteId;
    private String note_name;
    private String note_content;
    private String note_timestamp;
    private Double latitude;
    private Double longitude;
    private List<String> imageIds = new ArrayList<>();
    private Bitmap image;
    private List<String> imagePaths = new ArrayList<>();

    public NoteData(String userId, String note_name, String note_content, String note_timestamp, Double latitude, Double longitude, String noteId, List<String> imagrIds, List<String> imagePaths) {
        this.userId = userId;
        this.note_name = note_name;
        this.note_content = note_content;
        this.note_timestamp = note_timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageIds = imagrIds;
        this.imagePaths = imagePaths;
    }

    public NoteData(String uid, String name, String note_content, String timeStamp, double latitude, double longitude) {
    }

    public NoteData(String userId, String note_name, String note_content, String note_timestamp, Double latitude, Double longitude, String noteId) {
        this.userId = userId;
        this.note_name = note_name;
        this.note_content = note_content;
        this.note_timestamp = note_timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.noteId = noteId;
    }

    public NoteData(String userId,String note_name, String note_content, String note_timestamp, Double latitude, Double longitude, String noteId, String imageId){
        this.userId = userId;
        this.note_name = note_name;
        this.note_content = note_content;
        this.note_timestamp = note_timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.noteId = noteId;
        this.imageIds.add(imageId);

    }
    public NoteData(String userId,String note_name, String note_content, String note_timestamp, Double latitude, Double longitude, String noteId, String imageId, String imagePath){
        this.userId = userId;
        this.note_name = note_name;
        this.note_content = note_content;
        this.note_timestamp = note_timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.noteId = noteId;
        this.imageIds.add(imageId);
        this.imagePaths.add(imagePath);
    
    }


    public NoteData(String userId, String note_name, String note_content, String note_timestamp, Double latitude, Double longitude, String noteId, Bitmap image){
        this.userId = userId;
        this.note_name = note_name;
        this.note_content = note_content;
        this.note_timestamp = note_timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.noteId = noteId;
        this.image = image;
    }

    public NoteData(String uid, String title, String body, String date, double latitude, double longitude, String noteId, List<String> imagePaths) {
        this.userId = userId;
        this.note_name = note_name;
        this.note_content = note_content;
        this.note_timestamp = note_timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.noteId = noteId;
        for(int i=0; i<imagePaths.size();i++){
            this.imagePaths.add(imagePaths.get(i));
        }

    }




    public List<String> getImageIds() { return imageIds; }

    public void setImageId(String imageId, int index) {
        if(index < this.imageIds.size()){
            this.imageIds.remove(index);
            this.imageIds.add(index,imageId);
        }
       }

    public Bitmap getImage() { return image; }

    public void setImage(Bitmap image) { this.image = image; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNoteId() { return noteId; }

    public void setNoteId(String noteId) { this.noteId = noteId; }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getNote_name() {
        return note_name;
    }

    public void setNote_name(String note_name) {
        this.note_name = note_name;
    }

    public String getNote_content() {
        return note_content;
    }

    public void setNote_content(String note_content) {
        this.note_content = note_content;
    }

    public String getNote_timestamp() {
        return note_timestamp;
    }

    public void setNote_timestamp(String note_timestamp) {
        this.note_timestamp = note_timestamp;
    }

    public List<String> getImagePaths() { return imagePaths; }

    public void setImagePath(String imagePath) { this.imagePaths.add(imagePath); }
    public void setImagePaths( List<String> imagePaths ){
        this.imagePaths = imagePaths;
    }
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("title", note_name);
        result.put("content", note_content);
        result.put("timestamp", note_timestamp);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("imageIds",imageIds);


        return result;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */

}
