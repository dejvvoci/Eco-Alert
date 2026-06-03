package com.programimmobile.ecoalert.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class SentReport {

    @DocumentId
    private String id;
    private String reportId;
    private String category;
    private String description;
    private String institutionEmail;
    private String institutionName;
    private double latitude;
    private double longitude;
    private String adminId;

    @ServerTimestamp
    private Date sentAt;

    public SentReport() {}

    public SentReport(String reportId, String category, String description,
                      String institutionEmail, String institutionName,
                      double latitude, double longitude, String adminId) {
        this.reportId         = reportId;
        this.category         = category;
        this.description      = description;
        this.institutionEmail = institutionEmail;
        this.institutionName  = institutionName;
        this.latitude         = latitude;
        this.longitude        = longitude;
        this.adminId          = adminId;
    }

    public String getId()               { return id; }
    public String getReportId()         { return reportId; }
    public String getCategory()         { return category; }
    public String getDescription()      { return description; }
    public String getInstitutionEmail() { return institutionEmail; }
    public String getInstitutionName()  { return institutionName; }
    public double getLatitude()         { return latitude; }
    public double getLongitude()        { return longitude; }
    public String getAdminId()          { return adminId; }
    public Date getSentAt()             { return sentAt; }

    public void setId(String id)                           { this.id = id; }
    public void setReportId(String reportId)               { this.reportId = reportId; }
    public void setCategory(String category)               { this.category = category; }
    public void setDescription(String description)         { this.description = description; }
    public void setInstitutionEmail(String email)          { this.institutionEmail = email; }
    public void setInstitutionName(String name)            { this.institutionName = name; }
    public void setLatitude(double latitude)               { this.latitude = latitude; }
    public void setLongitude(double longitude)             { this.longitude = longitude; }
    public void setAdminId(String adminId)                 { this.adminId = adminId; }
    public void setSentAt(Date sentAt)                     { this.sentAt = sentAt; }
}