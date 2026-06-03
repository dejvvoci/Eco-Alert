package com.programimmobile.ecoalert.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {

    public static final String TYPE_APPROVED = "APPROVED";
    public static final String TYPE_REJECTED = "REJECTED";
    public static final String TYPE_SENT     = "SENT";

    @DocumentId
    private String id;
    private String userId;
    private String reportId;
    private String reportCategory;
    private String type;
    private String message;
    private boolean isRead;

    @ServerTimestamp
    private Date timestamp;

    // Konstruktor bosh — i nevojshëm për Firestore
    public Notification() {}

    public Notification(String userId, String reportId,
                        String reportCategory, String type, String message) {
        this.userId          = userId;
        this.reportId        = reportId;
        this.reportCategory  = reportCategory;
        this.type            = type;
        this.message         = message;
        this.isRead          = false;
    }

    // Getters
    public String getId()             { return id; }
    public String getUserId()         { return userId; }
    public String getReportId()       { return reportId; }
    public String getReportCategory() { return reportCategory; }
    public String getType()           { return type; }
    public String getMessage()        { return message; }
    public boolean isRead()           { return isRead; }
    public Date getTimestamp()        { return timestamp; }

    // Setters
    public void setId(String id)                     { this.id = id; }
    public void setUserId(String userId)             { this.userId = userId; }
    public void setReportId(String reportId)         { this.reportId = reportId; }
    public void setReportCategory(String c)          { this.reportCategory = c; }
    public void setType(String type)                 { this.type = type; }
    public void setMessage(String message)           { this.message = message; }
    public void setRead(boolean read)                { this.isRead = read; }
    public void setTimestamp(Date timestamp)         { this.timestamp = timestamp; }
}