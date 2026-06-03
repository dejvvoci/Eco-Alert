package com.programimmobile.ecoalert.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class Report {

    @DocumentId
    private String id;
    private String userId;
    private String category;
    private String description;
    private double latitude;
    private double longitude;
    private String status;
    private int confirmations;
    private String photoUrl;
    private List<String> photos;

    @ServerTimestamp
    private Date timestamp;

    // Konstruktor bosh — i nevojshëm për Firestore
    public Report() {}

    // Konstruktor kryesor
    public Report(String userId, String category, String description,
                  double latitude, double longitude) {
        this.userId = userId;
        this.category = category;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = "E re";
        this.confirmations = 0;
        this.photoUrl = null;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getStatus() { return status; }
    public int getConfirmations() { return confirmations; }
    public String getPhotoUrl() { return photoUrl; }
    public Date getTimestamp() { return timestamp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setStatus(String status) { this.status = status; }
    public void setConfirmations(int confirmations) { this.confirmations = confirmations; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public List<String> getPhotos() {
        return photos != null ? photos : new ArrayList<>();
    }
    public void setPhotos(List<String> photos) { this.photos = photos; }
}